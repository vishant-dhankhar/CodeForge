package com.codeforge.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private RedisSlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        rateLimiter = new RedisSlidingWindowRateLimiter(stringRedisTemplate);
    }

    @Test
    @DisplayName("Should allow request when count is below max limit")
    void shouldAllowRequestBelowLimit() {
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard("rate_limit:user_123")).thenReturn(2L); // 2 requests made out of 5 allowed
        when(zSetOperations.add(eq("rate_limit:user_123"), anyString(), anyDouble())).thenReturn(true);
        when(stringRedisTemplate.expire(eq("rate_limit:user_123"), any(Duration.class))).thenReturn(true);

        boolean allowed = rateLimiter.isAllowed("user_123", 5, 60);

        assertTrue(allowed);
    }

    @Test
    @DisplayName("Should block request when count reaches max limit")
    void shouldBlockRequestAtLimit() {
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard("rate_limit:user_123")).thenReturn(5L); // 5 requests already made (limit is 5)

        boolean allowed = rateLimiter.isAllowed("user_123", 5, 60);

        assertFalse(allowed);
    }

    @Test
    @DisplayName("Should fail open if Redis throws an unexpected exception to preserve service uptime")
    void shouldFailOpenOnRedisFailure() {
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Redis connection refused"));

        boolean allowed = rateLimiter.isAllowed("user_123", 5, 60);

        assertTrue(allowed);
    }
}
