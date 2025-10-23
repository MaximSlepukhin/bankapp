package com.github.maximslepukhin.service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.client.NotificationsClient;
import com.github.maximslepukhin.exception.OperationBlockedException;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final AccountsClient accountsClient;
    private final BlockerClient blockerClient;
    private final NotificationsClient notificationsClient;

    @Override
    public void deposit(CashOperationDto dto) {
        if (blockerClient.isBlocked(dto.getLogin(), dto.getCurrency(), dto.getAmount())) {
            throw new OperationBlockedException("Операция заблокирована");
        }

        accountsClient.updateBalance(dto.getLogin(), dto.getCurrency(), dto.getAmount());
        notificationsClient.notify(dto.getLogin(), "Пополнение на " + dto.getAmount() + " " + dto.getCurrency());
    }

    @Override
    public void withdraw(CashOperationDto dto) {
        BigDecimal currentBalance = accountsClient.getBalance(dto.getLogin(), dto.getCurrency());

        if (currentBalance.compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Недостаточно средств");
        }

        boolean blocked = blockerClient.isBlocked(dto.getLogin(), dto.getCurrency(), dto.getAmount());
        if (blocked) {
            throw new RuntimeException("Операция заблокирована");
        }

        accountsClient.updateBalance(dto.getLogin(), dto.getCurrency(), dto.getAmount().negate());
        notificationsClient.notify(dto.getLogin(), "Снятие " + dto.getAmount() + " " + dto.getCurrency());
    }
}

