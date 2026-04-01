package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.CashOperationDto;

import java.util.UUID;

public interface CashService {
    void deposit(CashOperationDto dto, UUID idempotencyKey);

    void withdraw(CashOperationDto dto, UUID idempotencyKey);
}