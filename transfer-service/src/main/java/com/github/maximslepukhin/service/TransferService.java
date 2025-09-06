package com.github.maximslepukhin.service;

import com.github.maximslepukhin.clients.AccountsClient;
import com.github.maximslepukhin.clients.BlockerClient;
import com.github.maximslepukhin.clients.ExchangeClient;
import com.github.maximslepukhin.clients.NotificationsClient;
import com.github.maximslepukhin.model.TransferRequest;
import com.github.maximslepukhin.model.TransferResponse;
import com.github.maximslepukhin.model.currency.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountsClient accountsClient;
    private final ExchangeClient exchangeClient;
    private final BlockerClient blockerClient;
    private final NotificationsClient notificationsClient;

    public TransferResponse transfer(TransferRequest request) {
        // Проверка блокировок
        if (blockerClient.isBlocked(request.getFromAccountId()) ||
            blockerClient.isBlocked(request.getToAccountId())) {
            return new TransferResponse(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    BigDecimal.ZERO,
                    "FAILED",
                    "One of the accounts is blocked"
            );
        }

        // Проверка баланса
        BigDecimal fromBalance = accountsClient.getBalance(request.getFromAccountId(), request.getCurrency());
        if (fromBalance.compareTo(request.getAmount()) < 0) {
            return new TransferResponse(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    BigDecimal.ZERO,
                    "FAILED",
                    "Insufficient balance"
            );
        }

        // Конвертация валюты при необходимости
        Currency fromCurrency = request.getCurrency();
        Currency toCurrency = accountsClient.getAccountCurrency(request.getToAccountId());
        BigDecimal convertedAmount = request.getAmount();
        if (fromCurrency != toCurrency) {
            convertedAmount = exchangeClient.convert(fromCurrency, toCurrency, request.getAmount());
        }

        // Списываем с отправителя
        accountsClient.debit(request.getFromAccountId(), request.getAmount(), fromCurrency);

        // Зачисляем получателю
        accountsClient.credit(request.getToAccountId(), convertedAmount, toCurrency);

        // Отправляем уведомления
        notificationsClient.notify(request.getFromAccountId(),
                "Transferred " + request.getAmount() + " " + fromCurrency + " to account " + request.getToAccountId());
        notificationsClient.notify(request.getToAccountId(),
                "Received " + convertedAmount + " " + toCurrency + " from account " + request.getFromAccountId());

        return new TransferResponse(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                convertedAmount,
                "SUCCESS",
                "Transfer completed successfully"
        );
    }
}
