package com.github.maximslepukhin.exception;


import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.model.enums.TransferStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransferBlockedException.class)
    public ResponseEntity<TransferResponse> handleBlocked(TransferBlockedException ex) {
        return ResponseEntity.badRequest().body(
                TransferResponse.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .status(TransferStatus.BLOCKED)
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MissingAccountException.class)
    public ResponseEntity<TransferResponse> handleMissingAccount(MissingAccountException ex) {
        return ResponseEntity.badRequest().body(
                TransferResponse.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .status(TransferStatus.FAILED)
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(OutboxSerializationException.class)
    public ResponseEntity<Map<String, String>> handleOutboxSerialization(OutboxSerializationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Внутренняя ошибка сервера"));
    }
}

