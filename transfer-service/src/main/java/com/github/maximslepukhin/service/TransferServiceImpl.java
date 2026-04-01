package com.github.maximslepukhin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.exception.CreditFailedAfterDebitException;
import com.github.maximslepukhin.exception.MissingAccountException;
import com.github.maximslepukhin.exception.OutboxSerializationException;
import com.github.maximslepukhin.exception.TransferBlockedException;
import com.github.maximslepukhin.model.dto.*;
import com.github.maximslepukhin.model.entity.OutboxEvent;
import com.github.maximslepukhin.model.entity.TransferEntity;
import com.github.maximslepukhin.model.enums.OutboxStatus;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.OutboxEventRepository;
import com.github.maximslepukhin.repository.TransferRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
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
    private final BlockerClient blockerClient;
    private final TransferRepository transferRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private Counter successfulTransfers;
    private Counter failedTransfers;

    @PostConstruct
    public void initMetrics() {
        successfulTransfers = Counter.builder("transfer.success")
                .description("Количество успешных переводов")
                .register(meterRegistry);

        failedTransfers = Counter.builder("transfer.failed")
                .description("Количество неуспешных переводов")
                .register(meterRegistry);
    }

    @Override
    @Retry(name = "transferService")
    @CircuitBreaker(name = "transferService", fallbackMethod = "fallbackTransfer")
    @Transactional
    public TransferResponse transfer(TransferRequest request, UUID idempotencyKey) {
        UUID txId = UUID.randomUUID();
        log.info("=== Transfer {} started: fromCurrency={} toCurrency={} ===",
                txId, request.getFromCurrency(), request.getToCurrency());

        // ✅ Проверка бизнес-правил
        if (request.getFromLogin().equals(request.getToLogin()) &&
            request.getFromCurrency().equals(request.getToCurrency())) {
            failedTransfers.increment();
            meterRegistry.counter("transfer_blocked_total",
                    "login", request.getFromLogin(),
                    "toLogin", request.getToLogin()).increment();
            throw new TransferBlockedException("Перевод самому себе в одной валюте невозможен");
        }

        // ✅ Проверка блокировки
        BlockerStatus status = blockerClient.check(new BlockerRequest(request.getFromLogin()));
        if (status.blocked()) {
            log.warn("Transfer {} blocked: {}", txId, status.reason());
            failedTransfers.increment();
            meterRegistry.counter("transfer_blocked_total",
                    "login", request.getFromLogin(),
                    "toLogin", request.getToLogin()).increment();
            throw new TransferBlockedException(status.reason());
        }

        // ✅ Проверяем наличие счетов
        List<String> fromCurrencies = accountsClient.getCurrencies(request.getFromLogin());
        List<String> toCurrencies = accountsClient.getCurrencies(request.getToLogin());

        if (!fromCurrencies.contains(request.getFromCurrency().name())) {
            failedTransfers.increment();
            throw new MissingAccountException("У отправителя '" + request.getFromLogin() + "' нет счёта в валюте " + request.getFromCurrency());
        }
        if (!toCurrencies.contains(request.getToCurrency().name())) {
            failedTransfers.increment();
            throw new MissingAccountException("У получателя '" + request.getToLogin() + "' нет счёта в валюте " + request.getToCurrency());
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

        // ✅ Движение денег (детерминированные ключи для идемпотентности при ретраях)
        UUID debitKey      = UUID.nameUUIDFromBytes((idempotencyKey + ":debit").getBytes());
        UUID creditKey     = UUID.nameUUIDFromBytes((idempotencyKey + ":credit").getBytes());
        UUID compensateKey = UUID.nameUUIDFromBytes((idempotencyKey + ":compensate").getBytes());

        accountsClient.debit(request.getFromLogin(), request.getFromCurrency().name(), debited, debitKey);

        try {
            accountsClient.credit(request.getToLogin(), request.getToCurrency().name(), credited, creditKey);
        } catch (Exception creditEx) {
            log.error("Transfer {}: credit failed — initiating saga retry/compensation. Reason: {}",
                    txId, creditEx.getMessage());
            throw new CreditFailedAfterDebitException(
                    creditEx.getMessage(),
                    request.getFromLogin(),
                    request.getFromCurrency().name(),
                    debited,
                    compensateKey
            );
        }

        // ✅ Сохраняем успешный перевод + outbox event атомарно
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

        try {
            NotificationRequest notificationRequest = new NotificationRequest(
                    request.getFromLogin(),
                    "Перевод на пользователя " + request.getToLogin() +
                    " на сумму " + credited + " " + request.getToCurrency()
            );
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Transfer")
                    .aggregateId(txId.toString())
                    .eventType("TRANSFER_COMPLETED")
                    .payload(objectMapper.writeValueAsString(notificationRequest))
                    .createdAt(Instant.now())
                    .status(OutboxStatus.PENDING)
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new OutboxSerializationException("Ошибка сериализации outbox payload для перевода " + txId, e);
        }

        log.info("Transfer {} completed successfully", txId);
        successfulTransfers.increment();
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

    private TransferResponse fallbackTransfer(TransferRequest request, UUID idempotencyKey, Throwable ex) {
        if (ex instanceof TransferBlockedException || ex instanceof MissingAccountException) {
            log.warn("Business exception, fallback не применяется: {}", ex.getMessage());
            throw (RuntimeException) ex;
        }

        UUID txId = UUID.randomUUID();
        failedTransfers.increment();

        // debit прошёл, credit упал после всех ретраев — запускаем компенсацию
        if (ex instanceof CreditFailedAfterDebitException creditEx) {
            return runCompensation(txId, request, creditEx);
        }

        // Сбой до debit (blocker, exchange и т.д.) — просто FAILED
        log.error("Transfer fallback {}: failed before debit, reason={}", txId, ex.getMessage());
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

        return TransferResponse.builder()
                .transactionId(txId.toString())
                .status(TransferStatus.FAILED)
                .debited(request.getAmount())
                .credited(BigDecimal.ZERO)
                .currencyFrom(request.getFromCurrency().name())
                .currencyTo(request.getToCurrency().name())
                .message("Перевод не выполнен: " + ex.getMessage())
                .build();
    }

    private TransferResponse runCompensation(UUID txId, TransferRequest request,
                                              CreditFailedAfterDebitException creditEx) {
        log.error("Transfer {}: all credit retries exhausted, running compensation for fromLogin={}",
                txId, creditEx.getFromLogin());
        try {
            accountsClient.compensate(
                    creditEx.getFromLogin(),
                    creditEx.getFromCurrency(),
                    creditEx.getDebited(),
                    creditEx.getCompensateKey()
            );

            log.info("Transfer {}: compensation SUCCESS, funds returned to {}", txId, creditEx.getFromLogin());
            TransferEntity entity = TransferEntity.builder()
                    .id(txId)
                    .fromAccountId(request.getFromLogin())
                    .toAccountId(request.getToLogin())
                    .debited(creditEx.getDebited())
                    .credited(BigDecimal.ZERO)
                    .currencyFrom(request.getFromCurrency().name())
                    .currencyTo(request.getToCurrency().name())
                    .status(TransferStatus.COMPENSATED)
                    .createdAt(Instant.now())
                    .compensationReason(creditEx.getMessage())
                    .build();
            transferRepository.save(entity);

            return TransferResponse.builder()
                    .transactionId(txId.toString())
                    .status(TransferStatus.COMPENSATED)
                    .debited(creditEx.getDebited())
                    .credited(BigDecimal.ZERO)
                    .currencyFrom(request.getFromCurrency().name())
                    .currencyTo(request.getToCurrency().name())
                    .message("Перевод отменён, средства возвращены")
                    .compensationReason(creditEx.getMessage())
                    .build();

        } catch (Exception compensateEx) {
            log.error("Transfer {}: COMPENSATION FAILED — REQUIRES MANUAL INTERVENTION. " +
                      "fromLogin={}, amount={}, creditError={}, compensateError={}",
                    txId, creditEx.getFromLogin(), creditEx.getDebited(),
                    creditEx.getMessage(), compensateEx.getMessage());

            String reason = "credit: " + creditEx.getMessage() + "; compensate: " + compensateEx.getMessage();
            TransferEntity entity = TransferEntity.builder()
                    .id(txId)
                    .fromAccountId(request.getFromLogin())
                    .toAccountId(request.getToLogin())
                    .debited(creditEx.getDebited())
                    .credited(BigDecimal.ZERO)
                    .currencyFrom(request.getFromCurrency().name())
                    .currencyTo(request.getToCurrency().name())
                    .status(TransferStatus.COMPENSATION_FAILED)
                    .createdAt(Instant.now())
                    .compensationReason(reason)
                    .build();
            transferRepository.save(entity);

            return TransferResponse.builder()
                    .transactionId(txId.toString())
                    .status(TransferStatus.COMPENSATION_FAILED)
                    .debited(creditEx.getDebited())
                    .credited(BigDecimal.ZERO)
                    .currencyFrom(request.getFromCurrency().name())
                    .currencyTo(request.getToCurrency().name())
                    .message("ТРЕБУЕТСЯ РУЧНОЕ ВМЕШАТЕЛЬСТВО")
                    .compensationReason(reason)
                    .build();
        }
    }
}
