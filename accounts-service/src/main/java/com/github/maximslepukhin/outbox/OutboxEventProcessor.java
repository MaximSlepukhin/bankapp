package com.github.maximslepukhin.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.config.kafka.KafkaUserRegistrationProducer;
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
    private final KafkaUserRegistrationProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    // Шаг 1: SELECT FOR UPDATE SKIP LOCKED + PROCESSING в одной транзакции
    // Блокировка защищает от параллельного захвата другим pod-ом,
    // коммит снимает блокировку и фиксирует PROCESSING
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

    // Шаг 2: отправка в Kafka — вне транзакции, БД-соединение не занято
    public void sendToKafka(OutboxEvent event) throws Exception {
        NotificationRequest notification = objectMapper.readValue(
                event.getPayload(), NotificationRequest.class);
        kafkaProducer.send(notification); // синхронный .get() внутри
    }

    // Шаг 3а: успех — помечаем SENT
    @Transactional
    public void markAsSent(OutboxEvent event) {
        event.setStatus(OutboxStatus.SENT);
        event.setProcessedAt(Instant.now());
        outboxEventRepository.save(event);
        log.info("Outbox event {} [{}] sent to Kafka", event.getId(), event.getEventType());
    }

    // Шаг 3б: ошибка — инкрементируем retryCount, возвращаем в PENDING или FAILED
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
            event.setStatus(OutboxStatus.PENDING); // вернули в очередь
            log.warn("Outbox event {} [{}] failed, retry {}/{}: {}",
                    event.getId(), event.getEventType(), retries, MAX_RETRIES, e.getMessage());
        }
        outboxEventRepository.save(event);
    }

    // Восстановление зависших событий: PROCESSING > 60 секунд → обратно в PENDING
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
