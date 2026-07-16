package com.turfconnect.turf.service;

import com.turfconnect.shared.cache.CacheKeyUtil;
import com.turfconnect.shared.cache.CacheProperties;
import com.turfconnect.shared.dto.PageResponse;
import com.turfconnect.turf.dto.TurfResponse;
import com.turfconnect.turf.dto.TurfSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TurfCacheService.
 *
 * Covers: cache hit, cache miss, cache write, eviction (detail + search pages),
 * graceful degradation when Redis is down, and concurrent access safety.
 */
@ExtendWith(MockitoExtension.class)
class TurfCacheServiceTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;
    @Mock private Cursor<String> scanCursor;

    private CacheProperties cacheProperties;
    private TurfCacheService turfCacheService;

    @BeforeEach
    void setUp() {
        cacheProperties = new CacheProperties();
        // Defaults: detail=600s, search=300s, reviews=300s
        turfCacheService = new TurfCacheService(redisTemplate, cacheProperties);
    }

    // ========================================
    // Single turf detail cache tests
    // ========================================

    @Test
    @DisplayName("getTurf: cache hit returns cached TurfResponse without MongoDB call")
    void getTurf_cacheHit_returnsCachedValue() {
        TurfResponse expected = TurfResponse.builder().id("turf-1").name("Green Arena").build();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(CacheKeyUtil.turfDetail("turf-1"))).thenReturn(expected);

        TurfResponse result = turfCacheService.getTurf("turf-1");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Green Arena");
        verify(valueOps).get(CacheKeyUtil.turfDetail("turf-1"));
    }

    @Test
    @DisplayName("getTurf: cache miss returns null")
    void getTurf_cacheMiss_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        TurfResponse result = turfCacheService.getTurf("turf-2");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("putTurf: stores TurfResponse with configured TTL")
    void putTurf_storesWithCorrectTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        TurfResponse response = TurfResponse.builder().id("turf-1").build();

        turfCacheService.putTurf("turf-1", response);

        verify(valueOps).set(
                eq(CacheKeyUtil.turfDetail("turf-1")),
                eq(response),
                eq(600L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("evictTurf: deletes detail key and SCAN-deletes all search page keys")
    void evictTurf_deletesBothDetailAndSearchPages() {
        when(redisTemplate.delete(CacheKeyUtil.turfDetail("turf-1"))).thenReturn(true);

        // Simulate SCAN returning two search page keys
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(scanCursor);
        when(scanCursor.hasNext()).thenReturn(true, true, false);
        when(scanCursor.next()).thenReturn("v1:cache:turfs:abc123", "v1:cache:turfs:def456");
        when(redisTemplate.delete(anySet())).thenReturn(2L);

        turfCacheService.evictTurf("turf-1");

        verify(redisTemplate).delete(CacheKeyUtil.turfDetail("turf-1"));
        verify(redisTemplate).delete(anySet());
    }

    // ========================================
    // Graceful degradation (Redis down)
    // ========================================

    @Test
    @DisplayName("getTurf: Redis connection failure returns null (graceful degradation)")
    void getTurf_redisDown_returnsNull() {
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        TurfResponse result = turfCacheService.getTurf("turf-1");

        assertThat(result).isNull();
        // No exception thrown — service degrades gracefully
    }

    @Test
    @DisplayName("putTurf: Redis connection failure is silently logged (no exception)")
    void putTurf_redisDown_noException() {
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        // Should not throw
        turfCacheService.putTurf("turf-1", TurfResponse.builder().id("turf-1").build());
    }

    @Test
    @DisplayName("evictTurf: Redis connection failure is silently logged (no exception)")
    void evictTurf_redisDown_noException() {
        when(redisTemplate.delete(anyString())).thenThrow(new RedisConnectionFailureException("Connection refused"));

        // Should not throw
        turfCacheService.evictTurf("turf-1");
    }

    // ========================================
    // Search page cache tests
    // ========================================

    @Test
    @DisplayName("getTurfsPage: cache hit returns cached page")
    @SuppressWarnings("unchecked")
    void getTurfsPage_cacheHit_returnsCachedPage() {
        PageResponse<TurfResponse> expected = PageResponse.<TurfResponse>builder()
                .content(List.of(TurfResponse.builder().id("turf-1").build()))
                .pageNumber(0).pageSize(10).totalElements(1).totalPages(1).last(true)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("v1:cache:turfs:somehash")).thenReturn(expected);

        PageResponse<TurfResponse> result = turfCacheService.getTurfsPage("v1:cache:turfs:somehash");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("buildSearchCacheKey: same criteria produces same key (deterministic)")
    void buildSearchCacheKey_deterministicForSameCriteria() {
        TurfSearchCriteria criteria = new TurfSearchCriteria();
        criteria.setCity("Chennai");
        criteria.setSport("Football");
        criteria.setPage(0);
        criteria.setSize(10);

        String key1 = turfCacheService.buildSearchCacheKey(criteria);
        String key2 = turfCacheService.buildSearchCacheKey(criteria);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1).startsWith("v1:cache:turfs:");
    }

    @Test
    @DisplayName("buildSearchCacheKey: different criteria produces different keys")
    void buildSearchCacheKey_differentCriteriaDifferentKeys() {
        TurfSearchCriteria criteria1 = new TurfSearchCriteria();
        criteria1.setCity("Chennai");

        TurfSearchCriteria criteria2 = new TurfSearchCriteria();
        criteria2.setCity("Mumbai");

        String key1 = turfCacheService.buildSearchCacheKey(criteria1);
        String key2 = turfCacheService.buildSearchCacheKey(criteria2);

        assertThat(key1).isNotEqualTo(key2);
    }

    // ========================================
    // Concurrent access test
    // ========================================

    @Test
    @DisplayName("Concurrent getTurf calls are safe (no race conditions)")
    void getTurf_concurrentAccess_threadSafe() throws InterruptedException {
        TurfResponse expected = TurfResponse.builder().id("turf-1").name("Concurrent Arena").build();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(expected);

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    TurfResponse result = turfCacheService.getTurf("turf-1");
                    if (result != null && "Concurrent Arena".equals(result.getName())) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    // ========================================
    // SCAN-based eviction test
    // ========================================

    @Test
    @DisplayName("evictAllTurfPages: uses SCAN (not KEYS) to find and delete search pages")
    void evictAllTurfPages_usesScan() {
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(scanCursor);
        when(scanCursor.hasNext()).thenReturn(true, false);
        when(scanCursor.next()).thenReturn("v1:cache:turfs:abc");
        when(redisTemplate.delete(anySet())).thenReturn(1L);

        turfCacheService.evictAllTurfPages();

        // Verify SCAN was used, NOT keys()
        verify(redisTemplate).scan(any(ScanOptions.class));
        verify(redisTemplate, never()).keys(anyString());
    }

    @Test
    @DisplayName("evictAllTurfPages: no-op when SCAN finds no matching keys")
    void evictAllTurfPages_noMatchingKeys_noDelete() {
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(scanCursor);
        when(scanCursor.hasNext()).thenReturn(false);

        turfCacheService.evictAllTurfPages();

        verify(redisTemplate, never()).delete(anySet());
    }
}
