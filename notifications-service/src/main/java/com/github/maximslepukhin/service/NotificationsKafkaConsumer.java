package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsKafkaConsumer {

    private final NotificationService notificationService;
    private final MeterRegistry meterRegistry;

    @KafkaListener(topics = "notifications", groupId = "notifications-service-consumer")
    public void consume(ConsumerRecord<String, NotificationRequest> record) {
        NotificationRequest request = record.value();
        String messageId = record.partition() + ":" + record.offset();
        try {
            notificationService.create(request, messageId);
            log.info("Notification saved for user {}: {}", request.getLogin(), request.getMessage());
        } catch (Exception e) {
            meterRegistry.counter("notification_failed_total",
                    "login", request.getLogin()
            ).increment();

            log.error("Failed to save notification for user {}: {}", request.getLogin(), e.getMessage());
        }
    }
}