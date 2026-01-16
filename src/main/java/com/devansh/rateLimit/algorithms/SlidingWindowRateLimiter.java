package com.devansh.rateLimit.algorithms;

import com.devansh.rateLimit.entity.RateLimitConfig;
import com.devansh.rateLimit.entity.RateLimitStats;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;

public class SlidingWindowRateLimiter implements RateLimiter {

    private final int limit;
    private final long windowMillis;
    private final Deque<Long> timeStamps;

    private final AtomicLong requestsAllowed = new AtomicLong(0);
    private final AtomicLong requestsNotAllowed = new AtomicLong(0);

    public SlidingWindowRateLimiter(RateLimitConfig config) {
        this.limit = config.getLimit();
        this.windowMillis = config.getWindowMillis();
        this.timeStamps = new ArrayDeque<>();
    }

    @Override
    public synchronized boolean isAllowed() {
        Long now = System.currentTimeMillis();

        while (!timeStamps.isEmpty() && now - timeStamps.peekFirst() >= windowMillis) {
            timeStamps.pollFirst();
        }

        if (timeStamps.size() >= limit) {
            requestsNotAllowed.incrementAndGet();
            return false;
        }

        timeStamps.addLast(now);
        requestsAllowed.incrementAndGet();
        return true;
    }

    @Override
    public RateLimitStats getStats() {
        return new RateLimitStats(requestsAllowed, requestsNotAllowed);
    }
}
