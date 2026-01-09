package com.devansh.service;

import com.devansh.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EmailProvider implements NotificationProvider {

    Random random = new Random();

    @Override
    public void send(Notification notification) {

        simulateLatency();

        if (random.nextInt(10) < 2) {
            throw new RuntimeException("20% chance error");
        }
    }

    private void simulateLatency() {
        try {
            Thread.sleep(50 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
