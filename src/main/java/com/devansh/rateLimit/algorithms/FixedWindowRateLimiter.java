package com.devansh.rateLimit.algorithms;

import com.devansh.rateLimit.entity.RateLimitConfig;
import com.devansh.rateLimit.entity.RateLimitStats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FixedWindowRateLimiter implements RateLimiter {

    private final int limit;
    private final int windowMillis;

    private volatile long windowStart;

    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicLong requestsAllowed = new AtomicLong(0);
    private final AtomicLong requestsNotAllowed = new AtomicLong(0);

    public FixedWindowRateLimiter(RateLimitConfig config) {
        this.limit = config.getLimit();
        this.windowMillis = config.getWindowMillis();
        this.windowStart = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean isAllowed() {
        long now = System.currentTimeMillis();

        if (now - windowStart > windowMillis) {
            windowStart = now;
            counter.set(0);
        }

        if (counter.incrementAndGet() <= limit) {
            requestsAllowed.incrementAndGet();
            return true;
        } else {
            requestsNotAllowed.incrementAndGet();
            return false;
        }
    }

    @Override
    public RateLimitStats getStats() {
        return new RateLimitStats(requestsAllowed, requestsNotAllowed);
    }
}
