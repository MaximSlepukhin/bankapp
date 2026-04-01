package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.entity.OutboxEvent;
import com.github.maximslepukhin.model.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    List<OutboxEvent> findByStatusAndProcessedAtBefore(OutboxStatus status, Instant cutoff);
}
