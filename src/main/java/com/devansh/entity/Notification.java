package com.devansh.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Notification {

    private String id;
    private String message;
    private String recipient;
    private NotificationType type;
    private Instant timestamp;

    private int retryCount;
    private long nextAttemptAt;

    public Notification(String message, String recipient, NotificationType type) {
        this.id = UUID.randomUUID().toString();
        this.message = message;
        this.recipient = recipient;
        this.type = type;
        this.timestamp = Instant.now();
        this.retryCount = 0;
    }

}
