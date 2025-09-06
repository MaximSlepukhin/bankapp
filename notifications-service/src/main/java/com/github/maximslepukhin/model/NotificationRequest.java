package com.github.maximslepukhin.model;

import com.github.maximslepukhin.model.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private String userId;   // ID пользователя
    private String message;  // текст уведомления
    private NotificationType type; // тип уведомления
}
