package com.github.maximslepukhin.idempotency;

import com.github.maximslepukhin.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupTask {

    private final IdempotencyKeyRepository repo;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpired() {
        log.info("Cleaning expired idempotency keys");
        repo.deleteExpired(Instant.now());
    }
}
