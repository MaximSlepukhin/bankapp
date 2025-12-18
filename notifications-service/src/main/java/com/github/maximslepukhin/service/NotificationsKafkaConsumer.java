package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications", groupId = "notifications-service-consumer")
    public void consume(NotificationRequest request) {
        notificationService.create(request);
        log.info("Notification saved for user {}: {}", request.getLogin(), request.getMessage());
    }
}