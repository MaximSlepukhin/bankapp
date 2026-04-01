package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.entity.IdempotencyKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

    Optional<IdempotencyKey> findByKeyAndExpiresAtAfter(UUID key, Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyKey i WHERE i.expiresAt < :now")
    void deleteExpired(@Param("now") Instant now);
}
