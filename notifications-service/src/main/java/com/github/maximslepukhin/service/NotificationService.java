package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification create(NotificationRequest request);
    List<Notification> getForUser(String login);
}
