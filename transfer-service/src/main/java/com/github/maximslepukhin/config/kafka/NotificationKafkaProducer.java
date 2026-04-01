package com.github.maximslepukhin.config.kafka;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    @Value("${kafka.topic.notifications:notifications}")
    private String topic;

    public void send(NotificationRequest notificationRequest) {
        try {
            kafkaTemplate.send(topic, notificationRequest.getLogin(), notificationRequest).get();
            log.info("Notification sent to Kafka: login={}", notificationRequest.getLogin());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka send interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}