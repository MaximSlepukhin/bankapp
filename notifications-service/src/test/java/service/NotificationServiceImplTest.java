package service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import com.github.maximslepukhin.service.NotificationServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final NotificationServiceImpl service = new NotificationServiceImpl(repository);

    @Test
    void create_shouldSaveNotification() {
        NotificationRequest request = new NotificationRequest("user1", "Hello!");

        Notification savedNotification = Notification.builder()
                .id(1L)
                .login(request.getLogin())
                .message(request.getMessage())
                .createdAt(OffsetDateTime.now())
                .build();

        when(repository.save(any(Notification.class))).thenReturn(savedNotification);

        Notification result = service.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLogin()).isEqualTo("user1");
        assertThat(result.getMessage()).isEqualTo("Hello!");
        verify(repository, times(1)).save(any(Notification.class));
    }
}
