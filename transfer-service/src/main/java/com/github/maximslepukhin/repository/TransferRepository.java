package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {
}
