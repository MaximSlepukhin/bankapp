package service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.service.NotificationService;
import com.github.maximslepukhin.service.NotificationsKafkaConsumer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotificationsKafkaConsumerTest {

    private NotificationService notificationService;
    private NotificationsKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        consumer = new NotificationsKafkaConsumer(notificationService, new SimpleMeterRegistry());
    }

    @Test
    void consume_shouldDeriveMessageIdFromPartitionAndOffset() {
        NotificationRequest request = new NotificationRequest("user1", "Hello");
        ConsumerRecord<String, NotificationRequest> record =
                new ConsumerRecord<>("notifications", 2, 15L, null, request);

        consumer.consume(record);

        verify(notificationService).create(request, "2:15");
    }

    @Test
    void consume_shouldNotThrow_whenServiceReturnsNull() {
        NotificationRequest request = new NotificationRequest("user1", "duplicate");
        ConsumerRecord<String, NotificationRequest> record =
                new ConsumerRecord<>("notifications", 0, 1L, null, request);

        when(notificationService.create(any(), any())).thenReturn(null);

        consumer.consume(record);

        verify(notificationService).create(request, "0:1");
    }

    @Test
    void consume_shouldIncrementCounter_whenServiceThrows() {
        NotificationRequest request = new NotificationRequest("user1", "bad");
        ConsumerRecord<String, NotificationRequest> record =
                new ConsumerRecord<>("notifications", 0, 5L, null, request);

        when(notificationService.create(any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        consumer.consume(record);

        verify(notificationService).create(request, "0:5");
    }
}
