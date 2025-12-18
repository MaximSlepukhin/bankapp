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

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${kafka.topic.notifications:notifications}")
    private String topicName;

    public void logKafkaConfig() {
        log.info("Bootstrap servers: {}", kafkaProperties.getBootstrapServers());
        log.info("Key serializer: {}", kafkaProperties.getProducer().getKeySerializer());
        log.info("Value serializer: {}", kafkaProperties.getProducer().getValueSerializer());
        log.info("Security protocol: {}", kafkaProperties.getProperties().get("security.protocol"));
        log.info("SASL mechanism: {}", kafkaProperties.getProperties().get("sasl.mechanism"));
        log.info("SASL JAAS config: {}", kafkaProperties.getProperties().get("sasl.jaas.config"));
    }

    public void send(NotificationRequest notificationRequest) {
        kafkaTemplate.send(topicName, notificationRequest);
        log.info("Notification sent to Kafka: login={}", notificationRequest.getLogin());
    }
}