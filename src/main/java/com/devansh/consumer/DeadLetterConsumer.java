package com.devansh.consumer;

import com.devansh.entity.Notification;
import com.devansh.queue.DeadLetterQueue;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterConsumer {

    private final DeadLetterQueue deadLetterQueue;

    public DeadLetterConsumer(DeadLetterQueue deadLetterQueue) {
        this.deadLetterQueue = deadLetterQueue;
    }

    @PostConstruct
    public void start() {
        Thread deadLetterThread = new Thread(() -> {
            while (true) {
                try {
                    Notification notification = deadLetterQueue.take();
                    log.error("DLQ notification {} after {} retries and current queue size: {}", notification.getId(), notification.getRetryCount(), deadLetterQueue.getQueueSize());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        deadLetterThread.setName("DLQ-Consumer");
        deadLetterThread.setDaemon(true);
        deadLetterThread.start();
    }

}
