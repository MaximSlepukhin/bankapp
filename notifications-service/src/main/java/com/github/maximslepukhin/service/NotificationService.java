package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;


public interface NotificationService {
    Notification create(NotificationRequest request);

}
