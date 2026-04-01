package service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import com.github.maximslepukhin.service.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class NotificationServiceImplTest {

    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final NotificationServiceImpl service = new NotificationServiceImpl(repository);

    @Test
    void create_shouldSaveNotification() {
        NotificationRequest request = new NotificationRequest("user1", "Hello!");
        String messageId = "0:42";

        Notification saved = Notification.builder()
                .id(1L)
                .login("user1")
                .message("Hello!")
                .messageId(messageId)
                .createdAt(OffsetDateTime.now())
                .build();

        when(repository.existsByMessageId(messageId)).thenReturn(false);
        when(repository.save(any(Notification.class))).thenReturn(saved);

        Notification result = service.create(request, messageId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLogin()).isEqualTo("user1");
        assertThat(result.getMessageId()).isEqualTo(messageId);
        verify(repository).save(any(Notification.class));
    }

    @Test
    void create_shouldReturnNull_WhenDuplicate() {
        NotificationRequest request = new NotificationRequest("user1", "Hello!");
        String messageId = "0:42";

        when(repository.existsByMessageId(messageId)).thenReturn(true);

        Notification result = service.create(request, messageId);

        assertThat(result).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldReturnNull_WhenRaceConditionOnSave() {
        NotificationRequest request = new NotificationRequest("user1", "Hello!");
        String messageId = "0:43";

        when(repository.existsByMessageId(messageId)).thenReturn(false);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

        Notification result = service.create(request, messageId);

        assertThat(result).isNull();
    }
}
