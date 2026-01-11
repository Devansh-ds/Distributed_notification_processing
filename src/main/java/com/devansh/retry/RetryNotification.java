package com.devansh.retry;

import com.devansh.entity.Notification;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

// entity used for retry queue
public class RetryNotification implements Delayed {

    private Notification notification;
    private long executeAt;

    public RetryNotification(Notification notification, long executeAt) {
        this.notification = notification;
        this.executeAt = executeAt;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(
                executeAt - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.executeAt, ((RetryNotification) other).executeAt);
    }

    public Notification getNotification() {
        return notification;
    }
}
