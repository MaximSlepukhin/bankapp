package com.github.maximslepukhin.config.security.kafka;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaUserRegistrationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${kafka.topic.notifications:notifications}")
    private String topicName;

    public void send(NotificationRequest notificationRequest) {
        kafkaTemplate.send(topicName, notificationRequest);
        log.info("Notification sent to Kafka: login={}", notificationRequest.getLogin());
    }
}