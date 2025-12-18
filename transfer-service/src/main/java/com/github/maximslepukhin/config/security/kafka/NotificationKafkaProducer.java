package com.github.maximslepukhin.config.security.kafka;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    private final String topic = "notifications";

    public void send(NotificationRequest notificationRequest) {
        kafkaTemplate.send(topic, notificationRequest.getLogin(), notificationRequest);
        log.info("Notification sent to Kafka: login={}", notificationRequest.getLogin());
    }
}