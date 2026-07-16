package com.turfconnect.shared.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised cache TTL settings.
 *
 * Bound from application.yml under the `cache` prefix so that
 * different environments (dev / staging / prod) can tune TTLs
 * without recompiling the service.
 *
 * Example application.yml snippet:
 *
 *   cache:
 *     turf-detail-ttl-seconds: 600        # 10 min
 *     turf-search-ttl-seconds: 300        # 5 min
 *     reviews-turf-ttl-seconds: 300       # 5 min
 */
@Data
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    /** TTL for a single turf detail cache entry (seconds). Default: 600 = 10 min. */
    private long turfDetailTtlSeconds = 600;

    /** TTL for a paginated turf search result cache entry (seconds). Default: 300 = 5 min. */
    private long turfSearchTtlSeconds = 300;

    /** TTL for the review list of a turf (seconds). Default: 300 = 5 min. */
    private long reviewsTurfTtlSeconds = 300;
}
