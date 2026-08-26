package com.codeforge.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSlidingWindowRateLimiter implements RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String fullKey = "rate_limit:" + key;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        try {
            // 1. Remove timestamps outside of the current rolling window
            stringRedisTemplate.opsForZSet().removeRangeByScore(fullKey, 0, windowStart);

            // 2. Count requests remaining in the rolling window
            Long requestCount = stringRedisTemplate.opsForZSet().zCard(fullKey);

            if (requestCount != null && requestCount >= maxRequests) {
                log.warn("Rate limit breached for key: {}. Current count: {}, Max allowed: {}",
                        key, requestCount, maxRequests);
                return false;
            }

            // 3. Record current request with a unique member ID (timestamp + UUID)
            String member = now + ":" + UUID.randomUUID().toString().substring(0, 8);
            stringRedisTemplate.opsForZSet().add(fullKey, member, (double) now);

            // 4. Set TTL so idle keys automatically clean up
            stringRedisTemplate.expire(fullKey, Duration.ofSeconds(windowSeconds + 5L));

            return true;
        } catch (Exception ex) {
            log.error("Redis rate limiter error for key: {}. Failing open to maintain availability.", key, ex);
            // In case of Redis transient connection failure, fail open to keep service accessible
            return true;
        }
    }
}
