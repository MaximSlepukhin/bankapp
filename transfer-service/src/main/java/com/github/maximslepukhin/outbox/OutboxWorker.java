package com.github.maximslepukhin.outbox;

import com.github.maximslepukhin.model.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {

    private final OutboxEventProcessor outboxEventProcessor;

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        outboxEventProcessor.recoverStuckEvents(); // PROCESSING > 60с → PENDING

        // TX1: SELECT FOR UPDATE SKIP LOCKED + все события → PROCESSING, commit
        List<OutboxEvent> events = outboxEventProcessor.fetchAndMarkAsProcessing();

        if (events.isEmpty()) {
            return;
        }

        log.info("Outbox: processing {} pending event(s)", events.size());

        for (OutboxEvent event : events) {
            try {
                outboxEventProcessor.sendToKafka(event);  // вне транзакции
                outboxEventProcessor.markAsSent(event);   // TX2: коммит SENT
            } catch (Exception e) {
                outboxEventProcessor.markAsFailed(event, e); // TX2: коммит PENDING/FAILED
            }
        }
    }
}
