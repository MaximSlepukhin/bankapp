package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.CashOperationDto;

public interface CashService {
    void deposit(CashOperationDto dto);

    void withdraw(CashOperationDto dto);
}