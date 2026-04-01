package com.github.maximslepukhin.idempotency;

import com.github.maximslepukhin.model.entity.IdempotencyKey;
import com.github.maximslepukhin.model.enums.IdempotencyStatus;
import com.github.maximslepukhin.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyKeyRepository repo;

    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> find(UUID key) {
        return repo.findByKeyAndExpiresAtAfter(key, Instant.now());
    }

    @Transactional
    public void reserve(UUID key, String operation) {
        try {
            repo.saveAndFlush(IdempotencyKey.builder()
                    .key(key)
                    .operation(operation)
                    .status(IdempotencyStatus.PROCESSING)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(86400))
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyConflictException("Duplicate idempotency key: " + key);
        }
    }

    @Transactional
    public void complete(UUID key, int httpStatus) {
        repo.findById(key).ifPresent(ik -> {
            ik.setStatus(IdempotencyStatus.COMPLETED);
            ik.setHttpStatus(httpStatus);
        });
    }

    @Transactional
    public void fail(UUID key, int httpStatus) {
        repo.findById(key).ifPresent(ik -> {
            ik.setStatus(IdempotencyStatus.FAILED);
            ik.setHttpStatus(httpStatus);
        });
    }
}
