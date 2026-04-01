package integration;

import com.github.maximslepukhin.NotificationsServiceApplication;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import com.github.maximslepukhin.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = NotificationsServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.auto-startup=false",
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0"
        }
)
@ActiveProfiles("test")
@Testcontainers
class NotificationsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("init-schema.sql");

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void clean() {
        notificationRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(notificationService).isNotNull();
    }

    @Test
    void create_shouldPersistNotification() {
        NotificationRequest request = new NotificationRequest("user1", "Перевод выполнен");

        Notification result = notificationService.create(request, "0:1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    @Test
    void create_shouldSkipDuplicate_whenSameMessageId() {
        NotificationRequest request = new NotificationRequest("user1", "Сообщение");

        notificationService.create(request, "0:5");
        Notification duplicate = notificationService.create(request, "0:5");

        assertThat(duplicate).isNull();
        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    @Test
    void create_shouldSaveBoth_whenDifferentMessageIds() {
        NotificationRequest request = new NotificationRequest("user1", "Сообщение");

        notificationService.create(request, "0:10");
        notificationService.create(request, "0:11");

        assertThat(notificationRepository.count()).isEqualTo(2);
    }

    @Test
    void create_shouldPersistMessageId() {
        NotificationRequest request = new NotificationRequest("user2", "Hello");

        notificationService.create(request, "1:42");

        Notification saved = notificationRepository.findAll().get(0);
        assertThat(saved.getMessageId()).isEqualTo("1:42");
        assertThat(saved.getLogin()).isEqualTo("user2");
    }
}
