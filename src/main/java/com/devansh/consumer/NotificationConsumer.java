package com.devansh.consumer;

import com.devansh.entity.Notification;
import com.devansh.queue.InMemoryNotificationQueue;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    private final InMemoryNotificationQueue notificationQueue;

    public NotificationConsumer(InMemoryNotificationQueue notificationQueue) {
        this.notificationQueue = notificationQueue;
    }

    public void startSingleConsumer() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    Notification notification = notificationQueue.take();
                    log.info("Processing notification: {}", notification);
                } catch (InterruptedException e) {
                    log.error("Interrupted Exception: {}", e.getMessage());
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    @PostConstruct
    public void startMultipleConsumer() {
        int workerCount = 20;
        int waitingTimeInMilliSeconds = 100;

        for (int i = 1; i <= workerCount; i++) {
            int workerId = i;

            Thread workerThread = new Thread(() -> {
                while (true) {
                    try {
                        Notification notification = notificationQueue.take();
                        Thread.sleep(waitingTimeInMilliSeconds);
                    } catch (InterruptedException e) {
                        log.error("Interrupted Exception for workerId: {} and message: {}", workerId, e.getMessage());
                    }
                }
            });
            workerThread.setDaemon(true);
            workerThread.start();

        }
    }

}
