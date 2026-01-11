package com.devansh.consumer;

import com.devansh.queue.InMemoryNotificationQueue;
import com.devansh.queue.RetryQueue;
import com.devansh.retry.RetryNotification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryDispatcher {

    private final RetryQueue retryQueue;
    private final InMemoryNotificationQueue notificationQueue;

    public RetryDispatcher(RetryQueue retryQueue, InMemoryNotificationQueue notificationQueue) {
        this.retryQueue = retryQueue;
        this.notificationQueue = notificationQueue;
    }

    @PostConstruct
    public void start() {
        Thread retryThread = new Thread(() -> {
            while (true) {
                try {
                    RetryNotification retryNotification = retryQueue.take();
                    notificationQueue.publish(retryNotification.getNotification());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        retryThread.setName("Retry-dispatcher");
        retryThread.setDaemon(true);
        retryThread.start();
    }

}
