package com.devansh.metrics;

import com.devansh.consumer.NotificationDispatcher;
import com.devansh.entity.Notification;
import com.devansh.queue.DeadLetterQueue;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemReporter {

    private final BlockingQueue<Notification> deadLetterQueue;
    private final NotificationMetrics metrics;
    private final ThreadPoolExecutor workerPool;

    @PostConstruct
    public void start() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("system-reporter");
            return thread;
        });
        scheduledExecutorService.scheduleAtFixedRate(this::report, 0, 2, TimeUnit.SECONDS);
    }

    private void report() {
        log.info(
                "POOL active={}, queued={}, completed={} | retries={} | success={} | failures={} | DLQ={}",
                workerPool.getActiveCount(),
                workerPool.getQueue().size(),
                workerPool.getCompletedTaskCount(),
                metrics.getRetryAttempt(),
                metrics.getSentSuccess(),
                metrics.getSentFailure(),
                metrics.getDeadLetterQueueCounter()
        );
    }
}














