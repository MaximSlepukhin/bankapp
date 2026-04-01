package idempotency;

import com.github.maximslepukhin.idempotency.IdempotencyConflictException;
import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.model.entity.IdempotencyKey;
import com.github.maximslepukhin.model.enums.IdempotencyStatus;
import com.github.maximslepukhin.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IdempotencyServiceTest {

    private IdempotencyKeyRepository repo;
    private IdempotencyService service;

    @BeforeEach
    void setUp() {
        repo = mock(IdempotencyKeyRepository.class);
        service = new IdempotencyService(repo);
    }

    @Test
    void find_shouldReturnEmpty_whenKeyNotFound() {
        UUID key = UUID.randomUUID();
        when(repo.findByKeyAndExpiresAtAfter(eq(key), any(Instant.class))).thenReturn(Optional.empty());

        Optional<IdempotencyKey> result = service.find(key);

        assertThat(result).isEmpty();
    }

    @Test
    void find_shouldReturnKey_whenKeyExists() {
        UUID key = UUID.randomUUID();
        IdempotencyKey ik = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.COMPLETED)
                .httpStatus(200)
                .build();
        when(repo.findByKeyAndExpiresAtAfter(eq(key), any(Instant.class))).thenReturn(Optional.of(ik));

        Optional<IdempotencyKey> result = service.find(key);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
    }

    @Test
    void reserve_shouldSaveKeyWithProcessingStatus() {
        UUID key = UUID.randomUUID();
        when(repo.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        service.reserve(key, "deposit");

        ArgumentCaptor<IdempotencyKey> captor = ArgumentCaptor.forClass(IdempotencyKey.class);
        verify(repo).saveAndFlush(captor.capture());
        IdempotencyKey saved = captor.getValue();

        assertThat(saved.getKey()).isEqualTo(key);
        assertThat(saved.getOperation()).isEqualTo("deposit");
        assertThat(saved.getStatus()).isEqualTo(IdempotencyStatus.PROCESSING);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void reserve_shouldThrowConflict_whenKeyAlreadyExists() {
        UUID key = UUID.randomUUID();
        when(repo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.reserve(key, "deposit"))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void complete_shouldSetStatusToCompleted() {
        UUID key = UUID.randomUUID();
        IdempotencyKey ik = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.PROCESSING)
                .build();
        when(repo.findById(key)).thenReturn(Optional.of(ik));

        service.complete(key, 200);

        assertThat(ik.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(ik.getHttpStatus()).isEqualTo(200);
    }

    @Test
    void fail_shouldSetStatusToFailed() {
        UUID key = UUID.randomUUID();
        IdempotencyKey ik = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.PROCESSING)
                .build();
        when(repo.findById(key)).thenReturn(Optional.of(ik));

        service.fail(key, 400);

        assertThat(ik.getStatus()).isEqualTo(IdempotencyStatus.FAILED);
        assertThat(ik.getHttpStatus()).isEqualTo(400);
    }

    @Test
    void complete_shouldDoNothing_whenKeyNotFound() {
        UUID key = UUID.randomUUID();
        when(repo.findById(key)).thenReturn(Optional.empty());

        assertThatCode(() -> service.complete(key, 200)).doesNotThrowAnyException();
    }
}
