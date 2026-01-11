package com.devansh.queue;

import com.devansh.entity.Notification;
import com.devansh.retry.RetryNotification;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

@Component
public class RetryQueue {
    private final DelayQueue<RetryNotification> delayQueue = new DelayQueue<>();

    public void schedule(Notification notification, long executeAt) {
        delayQueue.offer(new RetryNotification(notification, executeAt));
    }

    public RetryNotification take() throws InterruptedException {
        return delayQueue.take();
    }

}
