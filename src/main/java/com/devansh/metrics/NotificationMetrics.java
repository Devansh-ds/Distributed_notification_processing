package com.devansh.metrics;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class NotificationMetrics {

    private final AtomicLong retryAttempt = new AtomicLong(0);
    private final AtomicLong sentSuccess  = new AtomicLong(0);
    private final AtomicLong sentFailure  = new AtomicLong(0);

    public void incrementRetryAttempt() {
        retryAttempt.incrementAndGet();
    }

    public void incrementSentSuccess() {
        sentSuccess.incrementAndGet();
    }

    public void incrementSentFailure() {
        sentFailure.incrementAndGet();
    }

    public long getRetryAttempt() {
        return retryAttempt.get();
    }

    public long getSentSuccess() {
        return sentSuccess.get();
    }

    public long getSentFailure() {
        return sentFailure.get();
    }
}
