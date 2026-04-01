package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.CashOperationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "Cash", description = "Кассовые операции — внесение и снятие наличных")
public interface CashApi {

    @Operation(summary = "Внести наличные", description = "Зачисляет наличные средства на счёт пользователя")
    @ApiResponse(responseCode = "200", description = "Средства успешно зачислены")
    @ApiResponse(responseCode = "400", description = "Некорректные данные операции")
    @ApiResponse(responseCode = "403", description = "Операция заблокирована")
    @ApiResponse(responseCode = "409", description = "Запрос уже обрабатывается")
    @PostMapping("/deposit")
    ResponseEntity<Void> deposit(
            @RequestBody @Valid CashOperationDto dto,
            @Parameter(description = "Ключ идемпотентности — UUID, генерируется клиентом")
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey);

    @Operation(summary = "Снять наличные", description = "Списывает наличные средства со счёта пользователя")
    @ApiResponse(responseCode = "200", description = "Средства успешно сняты")
    @ApiResponse(responseCode = "400", description = "Недостаточно средств или некорректные данные")
    @ApiResponse(responseCode = "403", description = "Операция заблокирована")
    @ApiResponse(responseCode = "409", description = "Запрос уже обрабатывается")
    @PostMapping("/withdraw")
    ResponseEntity<Void> withdraw(
            @RequestBody @Valid CashOperationDto dto,
            @Parameter(description = "Ключ идемпотентности — UUID, генерируется клиентом")
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey);
}
