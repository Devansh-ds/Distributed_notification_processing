package com.devansh.service;

import com.devansh.entity.Notification;
import com.devansh.queue.InMemoryNotificationQueue;
import com.devansh.request.NotificationRequest;
import com.devansh.response.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {

    private final InMemoryNotificationQueue notificationQueue;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(InMemoryNotificationQueue notificationQueue) {
        this.notificationQueue = notificationQueue;
    }

    public NotificationResponse sendNotification(NotificationRequest notificationRequest) throws ResponseStatusException {
        if (notificationRequest.getType() == null || notificationRequest.getRecipient() == null) {
            throw new IllegalArgumentException("Notification Request type or recipient is null");
        }

        Notification notification = new Notification(
                notificationRequest.getMessage(),
                notificationRequest.getRecipient(),
                notificationRequest.getType()
        );

        boolean published = notificationQueue.publish(notification);
        return new NotificationResponse(notification.getId(), published);
    }

}
