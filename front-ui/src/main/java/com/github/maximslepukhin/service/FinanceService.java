package com.github.maximslepukhin.service;

import com.github.maximslepukhin.client.CashClient;
import com.github.maximslepukhin.client.TransferClient;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.dto.TransferRequestDto;
import com.github.maximslepukhin.model.enums.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceService {

    private final CashClient cashClient;
    private final TransferClient transferClient;

    public void deposit(String login, Currency currency, BigDecimal value) {
        cashClient.deposit(new CashOperationDto(login, currency, value));
    }

    public void withdraw(String login, Currency currency, BigDecimal value) {
        cashClient.withdraw(new CashOperationDto(login, currency, value));
    }

    public void transfer(String from, String to, Currency fromCurrency, Currency toCurrency, BigDecimal amount) {
        transferClient.transfer(new TransferRequestDto(from, to, fromCurrency, toCurrency, amount));
    }
}
