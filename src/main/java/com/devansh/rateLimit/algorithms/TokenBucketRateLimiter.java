package com.devansh.rateLimit.algorithms;

import com.devansh.rateLimit.entity.RateLimitConfig;
import com.devansh.rateLimit.entity.RateLimitStats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter implements RateLimiter {

    private final int capacity;
    private final int refillTokens;
    private final long refillIntervalMillis;

    private final AtomicLong requestsAllowed = new AtomicLong(0);
    private final AtomicLong requestsNotAllowed = new AtomicLong(0);

    private double tokens;
    private long lastRefillTimestamp;

    public TokenBucketRateLimiter(RateLimitConfig config) {
        this.capacity = config.getBucketCapacity();
        this.refillTokens = config.getRefillToken();
        this.refillIntervalMillis = config.getRefillIntervalMillis();
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean isAllowed() {
        refill();

        if (tokens >= 1) {
            tokens--;
            requestsAllowed.incrementAndGet();
            return true;
        }

        requestsNotAllowed.incrementAndGet();
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;

        if (elapsed >= refillIntervalMillis) {
            long intervals = elapsed / refillIntervalMillis;
            double refillAmount = intervals * refillTokens;

            tokens = Math.min(capacity, tokens + refillAmount);
            lastRefillTimestamp += intervals * refillIntervalMillis;
        }
    }

    @Override
    public RateLimitStats getStats() {
        return new RateLimitStats(requestsAllowed, requestsNotAllowed);
    }
}














