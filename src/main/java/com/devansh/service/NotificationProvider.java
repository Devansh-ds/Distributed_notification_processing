package com.devansh.service;

import com.devansh.entity.Notification;

public interface NotificationProvider {
    void send(Notification notification);
}

