package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.entity.IdempotencyKey;
import com.github.maximslepukhin.service.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashController implements CashApi {

    private final CashService cashService;
    private final IdempotencyService idempotencyService;

    @Override
    public ResponseEntity<Void> deposit(
            @RequestBody CashOperationDto dto,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey) {

        Optional<IdempotencyKey> existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            log.info("Idempotency key found: key={}, status={}", idempotencyKey, ik.getStatus());
            return switch (ik.getStatus()) {
                case COMPLETED -> ResponseEntity.ok().build();
                case FAILED -> ResponseEntity.status(ik.getHttpStatus()).build();
                case PROCESSING -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            };
        }

        idempotencyService.reserve(idempotencyKey, "deposit");

        try {
            cashService.deposit(dto, idempotencyKey);
            idempotencyService.complete(idempotencyKey, 200);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            idempotencyService.fail(idempotencyKey, 400);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> withdraw(
            @RequestBody CashOperationDto dto,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey) {

        Optional<IdempotencyKey> existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            log.info("Idempotency key found: key={}, status={}", idempotencyKey, ik.getStatus());
            return switch (ik.getStatus()) {
                case COMPLETED -> ResponseEntity.ok().build();
                case FAILED -> ResponseEntity.status(ik.getHttpStatus()).build();
                case PROCESSING -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            };
        }

        idempotencyService.reserve(idempotencyKey, "withdraw");

        try {
            cashService.withdraw(dto, idempotencyKey);
            idempotencyService.complete(idempotencyKey, 200);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            idempotencyService.fail(idempotencyKey, 400);
            throw e;
        }
    }
}
