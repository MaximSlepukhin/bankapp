package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationsService {

    public void sendNotification(NotificationRequest request) {
        // Здесь можно интегрировать email, SMS или push-уведомления
        // Для примера выводим в консоль
        log.info("[{}] Notification for user {}: {}", request.getType(), request.getUserId(), request.getMessage());
    }
}
