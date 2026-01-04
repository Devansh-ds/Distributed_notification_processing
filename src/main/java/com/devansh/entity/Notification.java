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

    public Notification(String message, String recipient, NotificationType type) {
        this.id = UUID.randomUUID().toString();
        this.message = message;
        this.recipient = recipient;
        this.type = type;
        this.timestamp = Instant.now();
    }

}
