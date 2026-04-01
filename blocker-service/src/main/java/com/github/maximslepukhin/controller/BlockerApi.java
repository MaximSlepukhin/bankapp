package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Blocker", description = "Проверка блокировки пользователя перед выполнением операций")
public interface BlockerApi {

    @Operation(summary = "Проверить блокировку", description = "Проверяет, заблокирован ли пользователь для выполнения финансовых операций")
    @ApiResponse(responseCode = "200", description = "Пользователь не заблокирован")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    @ApiResponse(responseCode = "403", description = "Пользователь заблокирован")
    @PostMapping("/check")
    ResponseEntity<BlockerStatus> check(@Valid @RequestBody BlockerRequest request);
}
