package com.turfconnect.turf.service;

import com.turfconnect.shared.cache.CacheKeyUtil;
import com.turfconnect.shared.cache.CacheProperties;
import com.turfconnect.shared.dto.PageResponse;
import com.turfconnect.turf.dto.TurfResponse;
import com.turfconnect.turf.dto.TurfSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Cache-aside layer for turf reads.
 *
 * Design principles (viva-ready):
 * 1. GRACEFUL DEGRADATION — every Redis call is wrapped in try-catch.
 *    If Redis is down the request falls through to MongoDB transparently.
 *    The user never sees a 500 because of a cache failure.
 * 2. SCAN over KEYS — wildcard eviction uses SCAN (O(1) per iteration)
 *    instead of KEYS (O(N), blocks the Redis event loop in production).
 * 3. VERSIONED KEYS — all keys start with "v1:" so a schema migration
 *    only requires bumping the version in CacheKeyUtil.
 * 4. EXTERNALISED TTLs — durations come from CacheProperties, not
 *    hardcoded constants. Different environments can tune via YAML.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TurfCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;

    // =============================================
    // Single turf detail cache
    // =============================================

    /**
     * Attempt to fetch a cached TurfResponse by ID.
     * Returns null on cache miss or Redis failure.
     */
    @SuppressWarnings("unchecked")
    public TurfResponse getTurf(String turfId) {
        String key = CacheKeyUtil.turfDetail(turfId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("CACHE HIT  — key={}", key);
                return (TurfResponse) cached;
            }
            log.debug("CACHE MISS — key={}", key);
        } catch (Exception e) {
            log.warn("CACHE FAILURE (get) — key={}, error={}", key, e.getMessage());
        }
        return null;
    }

    /** Store a TurfResponse in cache with the configured detail TTL. */
    public void putTurf(String turfId, TurfResponse response) {
        String key = CacheKeyUtil.turfDetail(turfId);
        try {
            redisTemplate.opsForValue().set(key, response,
                    cacheProperties.getTurfDetailTtlSeconds(), TimeUnit.SECONDS);
            log.debug("CACHE WRITE — key={}, ttl={}s", key, cacheProperties.getTurfDetailTtlSeconds());
        } catch (Exception e) {
            log.warn("CACHE FAILURE (put) — key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * Evict a single turf detail AND all search-page caches.
     *
     * Search pages are evicted because any turf mutation (update, delete,
     * rating change) could change the contents or ordering of any search
     * result page.
     */
    public void evictTurf(String turfId) {
        String detailKey = CacheKeyUtil.turfDetail(turfId);
        try {
            Boolean deleted = redisTemplate.delete(detailKey);
            log.info("CACHE EVICT — key={}, deleted={}", detailKey, deleted);
        } catch (Exception e) {
            log.warn("CACHE FAILURE (evict detail) — key={}, error={}", detailKey, e.getMessage());
        }
        evictAllTurfPages();
    }

    // =============================================
    // Paginated turf search cache
    // =============================================

    /**
     * Attempt to fetch a cached search result page.
     * Returns null on cache miss or Redis failure.
     */
    @SuppressWarnings("unchecked")
    public PageResponse<TurfResponse> getTurfsPage(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("CACHE HIT  — key={}", cacheKey);
                return (PageResponse<TurfResponse>) cached;
            }
            log.debug("CACHE MISS — key={}", cacheKey);
        } catch (Exception e) {
            log.warn("CACHE FAILURE (get page) — key={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    /** Store a paginated search result in cache with the search TTL. */
    public void putTurfsPage(String cacheKey, PageResponse<TurfResponse> page) {
        try {
            redisTemplate.opsForValue().set(cacheKey, page,
                    cacheProperties.getTurfSearchTtlSeconds(), TimeUnit.SECONDS);
            log.debug("CACHE WRITE — key={}, ttl={}s", cacheKey, cacheProperties.getTurfSearchTtlSeconds());
        } catch (Exception e) {
            log.warn("CACHE FAILURE (put page) — key={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * Evict ALL cached search pages using SCAN.
     *
     * SCAN iterates the keyspace incrementally with O(1) cost per call,
     * unlike KEYS which blocks the Redis event loop for the full scan.
     * We collect matching keys in batches and delete them.
     */
    public void evictAllTurfPages() {
        String pattern = CacheKeyUtil.turfSearchPattern();
        try {
            Set<String> keysToDelete = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keysToDelete.add(cursor.next());
                }
            }

            if (!keysToDelete.isEmpty()) {
                Long count = redisTemplate.delete(keysToDelete);
                log.info("CACHE EVICT — pattern={}, deletedCount={}", pattern, count);
            }
        } catch (Exception e) {
            log.warn("CACHE FAILURE (evict pages) — pattern={}, error={}", pattern, e.getMessage());
        }
    }

    // =============================================
    // Cache key generation for search criteria
    // =============================================

    /**
     * Generate a deterministic cache key from search criteria.
     *
     * We concatenate all filter fields into a stable string and SHA-256 hash it
     * to produce a fixed-length, collision-resistant key. The hash avoids
     * special characters and keeps keys short in Redis.
     */
    public String buildSearchCacheKey(TurfSearchCriteria criteria) {
        // Build a deterministic string from all search parameters
        StringBuilder sb = new StringBuilder();
        sb.append("city=").append(criteria.getCity());
        sb.append("&sport=").append(criteria.getSport());
        sb.append("&minPrice=").append(criteria.getMinPrice());
        sb.append("&maxPrice=").append(criteria.getMaxPrice());
        sb.append("&minRating=").append(criteria.getMinRating());
        sb.append("&surfaceType=").append(criteria.getSurfaceType());
        sb.append("&indoorOrOutdoor=").append(criteria.getIndoorOrOutdoor());
        sb.append("&floodlights=").append(criteria.getFloodlights());
        sb.append("&lng=").append(criteria.getLongitude());
        sb.append("&lat=").append(criteria.getLatitude());
        sb.append("&radius=").append(criteria.getRadiusInMeters());
        sb.append("&page=").append(criteria.getPage());
        sb.append("&size=").append(criteria.getSize());
        sb.append("&sortBy=").append(criteria.getSortBy());
        sb.append("&sortDir=").append(criteria.getSortDirection());

        String hash = sha256(sb.toString());
        return CacheKeyUtil.turfSearchPage(hash);
    }

    /**
     * SHA-256 hash of a string, returned as a hex string.
     * Falls back to the raw input if hashing fails (should never happen
     * since SHA-256 is mandated by the JVM spec).
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required by Java spec — this branch is unreachable
            log.error("SHA-256 not available — using raw criteria string as cache key", e);
            return input;
        }
    }
}
