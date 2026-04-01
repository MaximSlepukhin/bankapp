package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.annotation.ValidCurrency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "Accounts", description = "Управление счетами пользователей")
@Validated
public interface AccountApi {

    @Operation(summary = "Получить баланс", description = "Возвращает текущий баланс счёта пользователя в указанной валюте")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен")
    @ApiResponse(responseCode = "404", description = "Пользователь или счёт не найден")
    @GetMapping("/{login}/{currency}")
    ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login,
            @Parameter(description = "Валюта счёта", example = "USD") @ValidCurrency @PathVariable String currency
    );

    @Operation(summary = "Списать средства", description = "Списывает указанную сумму со счёта пользователя")
    @ApiResponse(responseCode = "200", description = "Средства успешно списаны")
    @ApiResponse(responseCode = "400", description = "Недостаточно средств")
    @ApiResponse(responseCode = "404", description = "Пользователь или счёт не найден")
    @PostMapping("/{login}/{currency}/debit")
    ResponseEntity<?> debit(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login,
            @Parameter(description = "Валюта счёта", example = "USD") @ValidCurrency @PathVariable String currency,
            @Parameter(description = "Сумма для списания", example = "500.00") @RequestParam BigDecimal amount,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey
    );

    @Operation(summary = "Зачислить средства", description = "Зачисляет указанную сумму на счёт пользователя")
    @ApiResponse(responseCode = "200", description = "Средства успешно зачислены")
    @ApiResponse(responseCode = "404", description = "Пользователь или счёт не найден")
    @PostMapping("/{login}/{currency}/deposit")
    ResponseEntity<?> credit(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login,
            @Parameter(description = "Валюта счёта", example = "USD") @ValidCurrency @PathVariable String currency,
            @Parameter(description = "Сумма для зачисления", example = "1000.00") @RequestParam BigDecimal amount,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey
    );

    @Operation(summary = "Обновить баланс", description = "Устанавливает новый баланс счёта пользователя")
    @ApiResponse(responseCode = "200", description = "Баланс успешно обновлён")
    @ApiResponse(responseCode = "404", description = "Пользователь или счёт не найден")
    @PostMapping("/{login}/{currency}")
    ResponseEntity<?> updateBalance(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login,
            @Parameter(description = "Валюта счёта", example = "USD") @ValidCurrency @PathVariable String currency,
            @RequestBody BigDecimal amount,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey
    );

    @Operation(summary = "Получить список валют", description = "Возвращает список валют, по которым у пользователя открыты счета")
    @ApiResponse(responseCode = "200", description = "Список валют успешно получен")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/{login}/currencies")
    ResponseEntity<List<String>> getCurrencies(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login
    );
}
