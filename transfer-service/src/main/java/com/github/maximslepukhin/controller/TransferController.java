package com.github.maximslepukhin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.model.entity.IdempotencyKey;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController implements TransferApi {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    private String serializeQuietly(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ResponseEntity<TransferResponse> transfer(
            @RequestBody @Valid TransferRequest request,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey) {

        Optional<IdempotencyKey> existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            log.info("Idempotency key found: key={}, status={}", idempotencyKey, ik.getStatus());
            return switch (ik.getStatus()) {
                case COMPLETED, FAILED -> {
                    try {
                        TransferResponse cached = objectMapper.readValue(ik.getResponseBody(), TransferResponse.class);
                        yield ResponseEntity.status(ik.getHttpStatus()).body(cached);
                    } catch (Exception e) {
                        yield ResponseEntity.status(ik.getHttpStatus()).build();
                    }
                }
                case PROCESSING -> ResponseEntity.status(HttpStatus.CONFLICT).body(
                        TransferResponse.builder()
                                .status(TransferStatus.FAILED)
                                .message("Request is already being processed")
                                .build()
                );
            };
        }

        idempotencyService.reserve(idempotencyKey, "transfer");

        try {
            TransferResponse response = transferService.transfer(request, idempotencyKey);
            String responseJson = serializeQuietly(response);
            idempotencyService.complete(idempotencyKey, 200, responseJson);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String errorJson = serializeQuietly(TransferResponse.builder()
                    .status(TransferStatus.FAILED)
                    .message(e.getMessage())
                    .build());
            idempotencyService.fail(idempotencyKey, 400, errorJson);
            throw e;
        }
    }
}
