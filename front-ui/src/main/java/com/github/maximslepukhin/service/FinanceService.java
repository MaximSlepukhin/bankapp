package com.github.maximslepukhin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.client.CashClient;
import com.github.maximslepukhin.client.TransferClient;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.dto.TransferRequestDto;
import com.github.maximslepukhin.model.enums.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceService {

    private final CashClient cashClient;
    private final TransferClient transferClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void deposit(String login, Currency currency, BigDecimal value) {
        try {
            cashClient.deposit(new CashOperationDto(login, currency, value));
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Ошибка Feign при депозите: status={}, body={}", e.getStatusCode(), responseBody);

            String message = extractErrorMessage(responseBody);
            log.error("Извлечённое сообщение ошибки: {}", message);

            throw new RuntimeException(message != null ? message : "Ошибка пополнения: " + e.getStatusCode());
        }
    }

    public void withdraw(String login, Currency currency, BigDecimal value) {
        try {
            cashClient.withdraw(new CashOperationDto(login, currency, value));
        } catch (HttpClientErrorException e) {
            String message = extractErrorMessage(e.getResponseBodyAsString());
            throw new RuntimeException(message != null ? message : "Ошибка списания: " + e.getStatusCode());
        }
    }

    public void transfer(String from, String to, Currency fromCurrency, Currency toCurrency, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительным числом");
        }
        try {
            transferClient.transfer(new TransferRequestDto(from, to, fromCurrency, toCurrency, amount));
        } catch (HttpClientErrorException e) {
            String message = extractErrorMessage(e.getResponseBodyAsString());
            throw new RuntimeException(message != null ? message : "Ошибка перевода: " + e.getStatusCode());
        }
    }

    public void cashOperation(String login, Currency currency, BigDecimal value, String action) {
        log.info("Начало cashOperation: login={}, currency={}, value={}, action={}",
                login, currency, value, action);

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Ошибка: неверная сумма: {}", value);
            throw new IllegalArgumentException("Сумма должна быть положительным числом");
        }

        switch (action.toUpperCase()) {
            case "PUT" -> {
                log.info("Выполняется депозит: login={}, currency={}, value={}", login, currency, value);
                deposit(login, currency, value);
                log.info("Депозит успешно выполнен: login={}, currency={}, value={}", login, currency, value);
            }
            case "GET" -> {
                log.info("Выполняется снятие: login={}, currency={}, value={}", login, currency, value);
                withdraw(login, currency, value);
                log.info("Снятие успешно выполнено: login={}, currency={}, value={}", login, currency, value);
            }
            default -> {
                log.warn("Неизвестное действие: {}", action);
                throw new IllegalArgumentException("Неизвестное действие: " + action);
            }
        }

        log.info("Завершение cashOperation для пользователя {}", login);
    }


    private String extractErrorMessage(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) return null;

            var node = objectMapper.readTree(responseBody);

            if (node.has("error")) {
                return node.get("error").asText();
            }
            if (node.has("reason")) {
                return node.get("reason").asText();
            }
            if (node.has("message")) {
                return node.get("message").asText();
            }

            return responseBody;
        } catch (Exception e) {
            log.warn("Не удалось разобрать тело ошибки: {}", responseBody);
            return responseBody;
        }
    }
}