package com.devansh.rateLimit;

import com.devansh.rateLimit.algorithms.FixedWindowRateLimiter;
import com.devansh.rateLimit.algorithms.RateLimiter;
import com.devansh.rateLimit.algorithms.SlidingWindowRateLimiter;
import com.devansh.rateLimit.algorithms.TokenBucketRateLimiter;
import com.devansh.rateLimit.entity.RateLimitConfig;
import com.devansh.rateLimit.entity.RateLimiterType;

public class RateLimitFactory {

    public static RateLimiter create(RateLimiterType type, RateLimitConfig config) {
        return switch (type) {
            case FIXED_WINDOW -> new FixedWindowRateLimiter(config);
            case SLIDING_WINDOW -> new SlidingWindowRateLimiter(config);
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(config);
        };
    }
}
