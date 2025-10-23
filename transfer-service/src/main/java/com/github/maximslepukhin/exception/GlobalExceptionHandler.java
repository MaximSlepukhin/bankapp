package com.github.maximslepukhin.exception;


import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.model.enums.TransferStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransferBlockedException.class)
    public ResponseEntity<TransferResponse> handleBlocked(TransferBlockedException ex) {
        return ResponseEntity.badRequest().body(
                TransferResponse.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .status(TransferStatus.BLOCKED)
                        .debited(null)
                        .credited(null)
                        .currencyFrom(null)
                        .currencyTo(null)
                        .message(ex.getMessage())
                        .build()
        );
    }
}
