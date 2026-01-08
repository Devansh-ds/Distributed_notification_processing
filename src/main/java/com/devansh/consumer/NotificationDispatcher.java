package com.devansh.consumer;

import com.devansh.entity.Notification;
import com.devansh.queue.InMemoryNotificationQueue;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NotificationDispatcher {

    private InMemoryNotificationQueue queue;
    private ExecutorService workerPool;
    private static final Integer CORE_POOL_SIZE = 10;
    private static final Integer MAXIMUM_POOL_SIZE = 20;
    private static final Integer KEEP_ALIVE_TIME = 60;

    public NotificationDispatcher(InMemoryNotificationQueue queue) {
        this.queue = queue;
        this.workerPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @PostConstruct
    public void start() {
        Thread dispatcherThread = new Thread(() -> {
            while (true) {
                try {
                    Notification notification = queue.take();
                    workerPool.submit(() -> {
                        process(notification);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(Thread.currentThread().getName() + " is interrupted");
                    break;
                }
            }
        });
        dispatcherThread.setName("dispatcher-thread");
        dispatcherThread.start();
    }

    private void process(Notification notification) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Worker interrupted while processing {}", notification.getId());
        }
        log.info("Processing notification {}", notification);
    }
}













