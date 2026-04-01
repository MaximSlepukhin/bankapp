package com.github.maximslepukhin.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.config.kafka.NotificationKafkaProducer;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.OutboxEvent;
import com.github.maximslepukhin.model.enums.OutboxStatus;
import com.github.maximslepukhin.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private static final int MAX_RETRIES = 3;
    private static final int STUCK_PROCESSING_SECONDS = 60;

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    // TX1: SELECT FOR UPDATE SKIP LOCKED + пометить PROCESSING — атомарно
    @Transactional
    public List<OutboxEvent> fetchAndMarkAsProcessing() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : events) {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
        }
        return events;
    }

    // Вне транзакции — БД-соединение не занято пока ждём Kafka
    public void sendToKafka(OutboxEvent event) throws Exception {
        NotificationRequest notification = objectMapper.readValue(
                event.getPayload(), NotificationRequest.class);
        kafkaProducer.send(notification);
    }

    // TX2: успех
    @Transactional
    public void markAsSent(OutboxEvent event) {
        event.setStatus(OutboxStatus.SENT);
        event.setProcessedAt(Instant.now());
        outboxEventRepository.save(event);
        log.info("Outbox event {} [{}] sent to Kafka", event.getId(), event.getEventType());
    }

    // TX2: ошибка — retry или FAILED
    @Transactional
    public void markAsFailed(OutboxEvent event, Exception e) {
        int retries = event.getRetryCount() + 1;
        event.setRetryCount(retries);
        if (retries >= MAX_RETRIES) {
            event.setStatus(OutboxStatus.FAILED);
            event.setProcessedAt(Instant.now());
            log.error("Outbox event {} [{}] permanently failed after {} retries: {}",
                    event.getId(), event.getEventType(), retries, e.getMessage());
        } else {
            event.setStatus(OutboxStatus.PENDING);
            log.warn("Outbox event {} [{}] failed, retry {}/{}: {}",
                    event.getId(), event.getEventType(), retries, MAX_RETRIES, e.getMessage());
        }
        outboxEventRepository.save(event);
    }

    // Восстановление зависших: PROCESSING > 60с → PENDING
    @Transactional
    public void recoverStuckEvents() {
        Instant cutoff = Instant.now().minusSeconds(STUCK_PROCESSING_SECONDS);
        List<OutboxEvent> stuck = outboxEventRepository
                .findByStatusAndProcessedAtBefore(OutboxStatus.PROCESSING, cutoff);
        for (OutboxEvent event : stuck) {
            event.setStatus(OutboxStatus.PENDING);
            outboxEventRepository.save(event);
            log.warn("Outbox event {} [{}] was stuck in PROCESSING, reset to PENDING",
                    event.getId(), event.getEventType());
        }
    }
}
