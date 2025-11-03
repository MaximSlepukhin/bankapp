package service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import com.github.maximslepukhin.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldSaveAndReturnNotification() {
        // given
        NotificationRequest request = new NotificationRequest("user1", "Hello!");
        Notification saved = Notification.builder()
                .id(1L)
                .login("user1")
                .message("Hello!")
                .createdAt(OffsetDateTime.now())
                .build();

        when(repository.save(any(Notification.class))).thenReturn(saved);

        // when
        Notification result = service.create(request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLogin()).isEqualTo("user1");
        assertThat(result.getMessage()).isEqualTo("Hello!");
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void getForUser_shouldReturnNotificationsFromRepository() {
        // given
        List<Notification> expected = List.of(
                new Notification(1L, "user1", "msg1", OffsetDateTime.now()),
                new Notification(2L, "user1", "msg2", OffsetDateTime.now())
        );
        when(repository.findByLoginOrderByCreatedAtDesc("user1")).thenReturn(expected);

        // when
        List<Notification> result = service.getForUser("user1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("msg1");
        verify(repository, times(1)).findByLoginOrderByCreatedAtDesc("user1");
    }
}
