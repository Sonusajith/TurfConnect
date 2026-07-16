package com.turfconnect.shared.cache;

/**
 * Centralized cache key generation for all services.
 *
 * All keys use a version prefix (v1) to enable instant cache-wide invalidation
 * after a schema change — just bump to v2 and all old keys become orphans that
 * expire naturally, with no explicit flush needed.
 *
 * Conventions:
 *   v1:cache:turf:{turfId}             - single turf detail (10 min TTL)
 *   v1:cache:turfs:{sha256-of-params}  - paginated turf search results (5 min TTL)
 *   v1:cache:reviews:turf:{turfId}     - all reviews for a turf (5 min TTL)
 */
public final class CacheKeyUtil {

    /** Version prefix — bump to v2 to silently invalidate all v1 entries. */
    private static final String VERSION = "v1";

    private CacheKeyUtil() {
        // Utility class — no instantiation
    }

    /** Single turf detail key. */
    public static String turfDetail(String turfId) {
        return VERSION + ":cache:turf:" + turfId;
    }

    /**
     * Turf search page key.
     * The hash is a hex string derived from the serialized search criteria
     * (city, sport, price range, etc.) so each unique parameter combination
     * maps to a distinct cache entry.
     */
    public static String turfSearchPage(String paramsHash) {
        return VERSION + ":cache:turfs:" + paramsHash;
    }

    /**
     * Pattern used with SCAN to find all cached search pages.
     * Never use KEYS in production — SCAN is O(1) per call and safe under load.
     */
    public static String turfSearchPattern() {
        return VERSION + ":cache:turfs:*";
    }

    /** All reviews for a specific turf. */
    public static String reviewsByTurf(String turfId) {
        return VERSION + ":cache:reviews:turf:" + turfId;
    }

    /**
     * Returns the version prefix. Useful for building new key categories
     * in future modules without hard-coding the version string.
     */
    public static String version() {
        return VERSION;
    }
}
