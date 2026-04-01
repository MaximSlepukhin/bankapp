package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
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

@Tag(name = "Transfers", description = "API для денежных переводов")
public interface TransferApi {

    @Operation(summary = "Совершить перевод", description = "Выполняет перевод средств между счетами пользователей с конвертацией валюты при необходимости")
    @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно")
    @ApiResponse(responseCode = "400", description = "Некорректные данные или недостаточно средств")
    @ApiResponse(responseCode = "403", description = "Операция заблокирована")
    @ApiResponse(responseCode = "409", description = "Запрос уже обрабатывается")
    @PostMapping
    ResponseEntity<TransferResponse> transfer(
            @RequestBody @Valid TransferRequest request,
            @Parameter(description = "Ключ идемпотентности — UUID, генерируется клиентом")
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey);
}
