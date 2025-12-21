package integration;

import com.github.maximslepukhin.NotificationsServiceApplication;
import com.github.maximslepukhin.repository.NotificationRepository;
import com.github.maximslepukhin.service.NotificationServiceImpl;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = NotificationsServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(topics = {"notifications"})
@ActiveProfiles("test")
class NotificationsKafkaConsumerTest {

    @MockBean
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void testNotificationConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "testGroup",
                "true",
                embeddedKafkaBroker
        );

        try (var consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer()
        ).createConsumer()) {

            consumer.subscribe(List.of("notifications"));
            kafkaTemplate.send("notifications", "user1", "New notification");

            var received = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "notifications",
                    Duration.ofSeconds(5)
            );

            assertThat(received.key()).isEqualTo("user1");
            assertThat(received.value()).isEqualTo("New notification");
        }
    }
}
