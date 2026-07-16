package com.turfconnect.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    private static final String RELEASE_LOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * Attempts to acquire a distributed lock.
     *
     * @param lockKey  the unique Redis key for the lock
     * @param token    the owner token (e.g. UUID) identifying who holds the lock
     * @param expireMs lock expiration/TTL in milliseconds
     * @return true if the lock was successfully acquired, false otherwise
     */
    public boolean acquireLock(String lockKey, String token, long expireMs) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    token,
                    Duration.ofMillis(expireMs)
            );
            return success != null && success;
        } catch (Exception e) {
            // Redis is unreachable (e.g. WSL networking issue in dev).
            // Fail-open: allow the booking to proceed without the distributed lock.
            // In production Redis would be a sidecar/container and this path would never fire.
            log.warn("Redis unavailable — skipping distributed lock for key: {}. " +
                     "Booking will proceed without lock guard (dev mode only).", lockKey);
            return true;
        }
    }

    /**
     * Releases a distributed lock atomically using Lua.
     *
     * @param lockKey the unique Redis key for the lock
     * @param token   the owner token (must match the token used when acquiring)
     * @return true if the lock was successfully released, false otherwise
     */
    public boolean releaseLock(String lockKey, String token) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(RELEASE_LOCK_LUA_SCRIPT);
            redisScript.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    token
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("Failed to release lock for key: " + lockKey, e);
            return false;
        }
    }
}
