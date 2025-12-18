package com.github.maximslepukhin.service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.config.security.kafka.KafkaNotificationProducer;
import com.github.maximslepukhin.exception.OperationBlockedException;
import com.github.maximslepukhin.exception.OperationFailedException;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final AccountsClient accountsClient;
    private final BlockerClient blockerClient;
    private final KafkaNotificationProducer kafkaProducer;

    @Override
    public void deposit(CashOperationDto dto) {
        if (blockerClient.isBlocked(dto.getLogin(), dto.getCurrency(), dto.getAmount())) {
            throw new OperationBlockedException("Операция заблокирована");
        }

        try {
            accountsClient.updateBalance(dto.getLogin(), dto.getCurrency(), dto.getAmount());
            kafkaProducer.send(new NotificationRequest(dto.getLogin(),
                    "Пополнение на " + dto.getAmount() + " " + dto.getCurrency()));
        } catch (HttpClientErrorException e) {
            String message = extractErrorMessage(e.getResponseBodyAsString());
            throw new OperationFailedException(message != null ? message :
                    "Ошибка от accounts-service: " + e.getStatusCode());
        } catch (Exception e) {
            throw new OperationFailedException("Ошибка при обращении к сервису счетов: " + e.getMessage());
        }
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
        kafkaProducer.send(new NotificationRequest(dto.getLogin(),
                "Снятие " + dto.getAmount() + " " + dto.getCurrency()));
    }

    private String extractErrorMessage(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) return null;
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var json = mapper.readTree(responseBody);
            return json.has("error") ? json.get("error").asText() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}

