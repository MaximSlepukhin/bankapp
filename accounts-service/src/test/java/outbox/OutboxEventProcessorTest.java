package outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.config.kafka.KafkaUserRegistrationProducer;
import com.github.maximslepukhin.model.entity.OutboxEvent;
import com.github.maximslepukhin.model.enums.OutboxStatus;
import com.github.maximslepukhin.outbox.OutboxEventProcessor;
import com.github.maximslepukhin.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxEventProcessorTest {

    private OutboxEventRepository repo;
    private KafkaUserRegistrationProducer kafkaProducer;
    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        repo = mock(OutboxEventRepository.class);
        kafkaProducer = mock(KafkaUserRegistrationProducer.class);
        processor = new OutboxEventProcessor(repo, kafkaProducer, new ObjectMapper());
    }

    private OutboxEvent pendingEvent() {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("Transfer")
                .aggregateId(UUID.randomUUID().toString())
                .eventType("TRANSFER_COMPLETED")
                .payload("{\"login\":\"user1\",\"message\":\"done\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void fetchAndMarkAsProcessing_shouldChangeStatusToProcessing() {
        OutboxEvent event = pendingEvent();
        when(repo.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(event));

        List<OutboxEvent> result = processor.fetchAndMarkAsProcessing();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSING);
        verify(repo).save(event);
    }

    @Test
    void fetchAndMarkAsProcessing_shouldReturnEmpty_whenNoPendingEvents() {
        when(repo.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of());

        List<OutboxEvent> result = processor.fetchAndMarkAsProcessing();

        assertThat(result).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void markAsSent_shouldSetStatusToSent() {
        OutboxEvent event = pendingEvent();

        processor.markAsSent(event);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.SENT);
        verify(repo).save(event);
    }

    @Test
    void markAsFailed_shouldReturnToPending_whenBelowMaxRetries() {
        OutboxEvent event = pendingEvent();
        event.setRetryCount(0);

        processor.markAsFailed(event, new RuntimeException("Kafka down"));

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(1);
        verify(repo).save(event);
    }

    @Test
    void markAsFailed_shouldSetPermanentlyFailed_whenMaxRetriesReached() {
        OutboxEvent event = pendingEvent();
        event.setRetryCount(2);

        processor.markAsFailed(event, new RuntimeException("Kafka down"));

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(3);
        verify(repo).save(event);
    }

    @Test
    void recoverStuckEvents_shouldResetStuckEventsBackToPending() {
        OutboxEvent stuckEvent = pendingEvent();
        stuckEvent.setStatus(OutboxStatus.PROCESSING);
        stuckEvent.setProcessedAt(Instant.now().minusSeconds(120));

        when(repo.findByStatusAndProcessedAtBefore(eq(OutboxStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckEvent));

        processor.recoverStuckEvents();

        assertThat(stuckEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        verify(repo).save(stuckEvent);
    }

    @Test
    void sendToKafka_shouldDeserializeAndSendToKafka() throws Exception {
        OutboxEvent event = pendingEvent();

        processor.sendToKafka(event);

        verify(kafkaProducer).send(argThat(req ->
                req.getLogin().equals("user1") && req.getMessage().equals("done")));
    }
}
