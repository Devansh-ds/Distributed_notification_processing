package com.devansh.queue;

import com.devansh.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DeadLetterQueue {

    private BlockingQueue<Notification> deadLetterQueue = new LinkedBlockingQueue<>(1000);

    public boolean publish(Notification notification) {
        return deadLetterQueue.offer(notification);
    }

    public Notification take() throws InterruptedException {
        return deadLetterQueue.take();
    }

    public BlockingQueue<Notification> getDeadLetterQueue() {
        return deadLetterQueue;
    }

    public int getQueueSize() {
        return deadLetterQueue.size();
    }

}
