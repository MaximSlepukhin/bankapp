package com.github.maximslepukhin.service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.client.NotificationsClient;
import com.github.maximslepukhin.exception.TransferBlockedException;
import com.github.maximslepukhin.model.dto.*;
import com.github.maximslepukhin.model.entity.TransferEntity;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.TransferRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final AccountsClient accountsClient;
    private final ExchangeClient exchangeClient;
    private final NotificationsClient notificationsClient;
    private final BlockerClient blockerClient;
    private final TransferRepository transferRepository;

    @Override
    @Retry(name = "transferService")
    @CircuitBreaker(name = "transferService", fallbackMethod = "fallbackTransfer")
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        UUID txId = UUID.randomUUID();
        log.info("=== Transfer {} started ===", txId);
        log.info("From={} To={} Amount={} FromCurrency={} ToCurrency={}",
                request.getFromLogin(), request.getToLogin(), request.getAmount(),
                request.getFromCurrency(), request.getToCurrency());

        // ✅ Проверка бизнес-правил
        if (request.getFromLogin().equals(request.getToLogin()) &&
            request.getFromCurrency().equals(request.getToCurrency())) {
            throw new TransferBlockedException("Перевод самому себе в одной валюте невозможен");
        }

        // ✅ Проверка блокировки
        BlockerStatus status = blockerClient.check(new BlockerRequest(request.getFromLogin()));
        if (status.blocked()) {
            log.warn("Transfer {} blocked: {}", txId, status.reason());
            throw new TransferBlockedException(status.reason());
        }

        // ✅ Проверяем наличие счетов
        List<String> fromCurrencies = accountsClient.getCurrencies(request.getFromLogin());
        List<String> toCurrencies = accountsClient.getCurrencies(request.getToLogin());

        if (!fromCurrencies.contains(request.getFromCurrency().name())) {
            throw new IllegalArgumentException("У отправителя нет счёта в валюте " + request.getFromCurrency());
        }
        if (!toCurrencies.contains(request.getToCurrency().name())) {
            throw new IllegalArgumentException("У получателя нет счёта в валюте " + request.getToCurrency());
        }

        // ✅ Конвертация валют
        BigDecimal debited = request.getAmount();
        BigDecimal credited = debited;

        if (!request.getFromCurrency().equals(request.getToCurrency())) {
            ConvertResponse response = exchangeClient.convert(new ConvertRequest(
                    debited,
                    request.getFromCurrency(),
                    request.getToCurrency()
            ));

            credited = response.getConverted();
            log.info("Transfer {}: converted {} {} → {} {}", txId,
                    debited, request.getFromCurrency(), credited, request.getToCurrency());
        } else {
            log.info("Transfer {}: no conversion needed", txId);
        }

        // ✅ Движение денег
        accountsClient.debit(request.getFromLogin(), request.getFromCurrency().name(), debited);
        accountsClient.credit(request.getToLogin(), request.getToCurrency().name(), credited);

        // ✅ Уведомление
        try {
            notificationsClient.notify(new NotificationRequest(
                    request.getFromLogin(),
                    "Перевод на пользователя " + request.getToLogin() +
                    " на сумму " + credited + " " + request.getToCurrency()
            ));
        } catch (Exception e) {
            log.error("Transfer {}: уведомление не доставлено: {}", txId, e.getMessage());
        }

        // ✅ Сохраняем успешный перевод
        TransferEntity entity = TransferEntity.builder()
                .id(txId)
                .fromAccountId(request.getFromLogin())
                .toAccountId(request.getToLogin())
                .debited(debited)
                .credited(credited)
                .currencyFrom(request.getFromCurrency().name())
                .currencyTo(request.getToCurrency().name())
                .status(TransferStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();

        transferRepository.save(entity);

        log.info("Transfer {} completed successfully", txId);

        return TransferResponse.builder()
                .transactionId(txId.toString())
                .status(TransferStatus.SUCCESS)
                .debited(debited)
                .credited(credited)
                .currencyFrom(request.getFromCurrency().name())
                .currencyTo(request.getToCurrency().name())
                .message("Transfer successful")
                .build();
    }

    //  fallback вызывается только при сбоях внешних микросервисов
    private TransferResponse fallbackTransfer(TransferRequest request, Throwable ex) {
        // Если это бизнес-ошибка — пробрасываем дальше
        if (ex instanceof TransferBlockedException || ex instanceof IllegalArgumentException) {
            log.warn("Business exception, fallback не применяется: {}", ex.getMessage());
            throw (RuntimeException) ex;
        }

        UUID txId = UUID.randomUUID();
        log.error("Transfer fallback {}, причина={}, тип={}",
                txId, ex.getMessage(), ex.getClass().getName());

        // Сохраняем запись об ошибке
        TransferEntity entity = TransferEntity.builder()
                .id(txId)
                .fromAccountId(request.getFromLogin())
                .toAccountId(request.getToLogin())
                .debited(request.getAmount())
                .credited(BigDecimal.ZERO)
                .currencyFrom(request.getFromCurrency().name())
                .currencyTo(request.getToCurrency().name())
                .status(TransferStatus.FAILED)
                .createdAt(Instant.now())
                .build();

        transferRepository.save(entity);

        // Возвращаем безопасный ответ
        return TransferResponse.builder()
                .transactionId(txId.toString())
                .status(TransferStatus.FAILED)
                .debited(request.getAmount())
                .credited(BigDecimal.ZERO)
                .currencyFrom(request.getFromCurrency().name())
                .currencyTo(request.getToCurrency().name())
                .message("Transfer failed due to temporary service unavailability: " + ex.getMessage())
                .build();
    }
}