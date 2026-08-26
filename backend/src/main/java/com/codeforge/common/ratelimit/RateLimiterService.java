package com.codeforge.common.ratelimit;

public interface RateLimiterService {

    boolean isAllowed(String key, int maxRequests, int windowSeconds);
}
