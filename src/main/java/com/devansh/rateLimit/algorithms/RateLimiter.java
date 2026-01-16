package com.devansh.rateLimit.algorithms;

import com.devansh.rateLimit.entity.RateLimitStats;

public interface RateLimiter {
    boolean isAllowed();
    RateLimitStats getStats();
}
