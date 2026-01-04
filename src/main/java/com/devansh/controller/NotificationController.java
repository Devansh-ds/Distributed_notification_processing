package com.devansh.controller;

import com.devansh.request.NotificationRequest;
import com.devansh.response.NotificationResponse;
import com.devansh.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        NotificationResponse notificationResponse = notificationService.sendNotification(notificationRequest);

        if (!notificationResponse.isPublished()) {
            return ResponseEntity.status(429).body(notificationResponse);
        }

        return new ResponseEntity<>(notificationResponse, HttpStatus.ACCEPTED);
    }

}
