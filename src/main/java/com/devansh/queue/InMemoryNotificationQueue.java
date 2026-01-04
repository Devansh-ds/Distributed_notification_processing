package com.devansh.queue;

import com.devansh.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class InMemoryNotificationQueue {

    private BlockingQueue<Notification> queue = new LinkedBlockingQueue<>(1000);

    public boolean publish(Notification notification) {
        return queue.offer(notification);
    }

    public Notification take() throws InterruptedException {
        return queue.take();
    }

}
