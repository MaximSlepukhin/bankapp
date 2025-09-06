package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.CashOperationRequest;
import com.github.maximslepukhin.model.AccountBalance;
import com.github.maximslepukhin.repository.AccountBalanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CashService {

    private final AccountBalanceRepository repository;

    public CashService(AccountBalanceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AccountBalance deposit(CashOperationRequest request) {
        AccountBalance balance = repository.findByAccountIdAndCurrency(request.getAccountId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("Счёт не найден"));

        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        return repository.save(balance);
    }

    @Transactional
    public AccountBalance withdraw(CashOperationRequest request) {
        AccountBalance balance = repository.findByAccountIdAndCurrency(request.getAccountId(), request.getCurrency())
                .orElseThrow(() -> new RuntimeException("Счёт не найден"));

        if (balance.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Недостаточно средств");
        }

        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        return repository.save(balance);
    }
}