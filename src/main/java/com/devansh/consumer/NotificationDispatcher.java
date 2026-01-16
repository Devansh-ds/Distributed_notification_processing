package com.devansh.consumer;

import com.devansh.entity.Notification;
import com.devansh.metrics.NotificationMetrics;
import com.devansh.queue.DeadLetterQueue;
import com.devansh.queue.InMemoryNotificationQueue;
import com.devansh.queue.RetryQueue;
import com.devansh.retry.RetryPolicy;
import com.devansh.service.NotificationProvider;
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
    private NotificationProvider notificationProvider;
    private DeadLetterQueue deadLetterQueue;
    private RetryQueue retryQueue;
    private NotificationMetrics notificationMetrics;

    private static final Integer CORE_POOL_SIZE = 10;
    private static final Integer MAXIMUM_POOL_SIZE = 20;
    private static final Integer KEEP_ALIVE_TIME = 60;

    public NotificationDispatcher(InMemoryNotificationQueue queue,
                                  NotificationProvider notificationProvider,
                                  DeadLetterQueue deadLetterQueue,
                                  RetryQueue retryQueue,
                                  NotificationMetrics notificationMetrics) {
        this.queue = queue;
        this.workerPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                new ThreadPoolExecutor.AbortPolicy()
        );
        this.notificationProvider = notificationProvider;
        this.deadLetterQueue = deadLetterQueue;
        this.retryQueue = retryQueue;
        this.notificationMetrics = notificationMetrics;
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
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
    }

    private void process(Notification notification) {
        try {
            notificationProvider.send(notification);
            notificationMetrics.incrementSentSuccess();
        } catch (Exception e) {
            notificationMetrics.incrementSentFailure();

            int retry = notification.getRetryCount() + 1;
            notification.setRetryCount(retry);
            notificationMetrics.incrementSentFailure();
            notificationMetrics.incrementRetryAttempt();

            if (retry > RetryPolicy.MAX_RETRIES) {
                deadLetterQueue.publish(notification);
                notificationMetrics.incrementDeadLetterQueueCounter();
                return;
            }

            long delay = RetryPolicy.backOffMillis(retry);
            long executeAt = System.currentTimeMillis() + delay;

            retryQueue.schedule(notification, executeAt);
        }
    }

    public ThreadPoolExecutor getWorkerPool() {
        return (ThreadPoolExecutor) workerPool;
    }
}













