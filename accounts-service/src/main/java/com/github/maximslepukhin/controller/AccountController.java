package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.model.entity.IdempotencyKey;
import com.github.maximslepukhin.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController implements AccountApi {

    private final AccountService accountService;
    private final IdempotencyService idempotencyService;

    @Override
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String login, @PathVariable String currency) {
        return ResponseEntity.ok(accountService.getBalance(login, currency));
    }

    @Override
    public ResponseEntity<?> debit(@PathVariable String login, @PathVariable String currency,
                                   @RequestParam BigDecimal amount,
                                   @RequestHeader("X-Idempotency-Key") UUID idempotencyKey) {
        Optional<IdempotencyKey> existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            return switch (ik.getStatus()) {
                case COMPLETED -> ResponseEntity.ok().build();
                case FAILED -> ResponseEntity.status(ik.getHttpStatus()).build();
                case PROCESSING -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            };
        }
        idempotencyService.reserve(idempotencyKey, "debit");
        try {
            accountService.debit(login, currency, amount);
            idempotencyService.complete(idempotencyKey, 200);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            idempotencyService.fail(idempotencyKey, 400);
            throw e;
        }
    }

    @Override
    public ResponseEntity<?> credit(@PathVariable String login, @PathVariable String currency,
                                    @RequestParam BigDecimal amount,
                                    @RequestHeader("X-Idempotency-Key") UUID idempotencyKey) {
        Optional<IdempotencyKey> existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            return switch (ik.getStatus()) {
                case COMPLETED -> ResponseEntity.ok().build();
                case FAILED -> ResponseEntity.status(ik.getHttpStatus()).build();
                case PROCESSING -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            };
        }
        idempotencyService.reserve(idempotencyKey, "credit");
        try {
            accountService.credit(login, currency, amount);
            idempotencyService.complete(idempotencyKey, 200);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            idempotencyService.fail(idempotencyKey, 400);
            throw e;
        }
    }

    @Override
    public ResponseEntity<?> updateBalance(@PathVariable String login, @PathVariable String currency,
                                           @RequestBody BigDecimal amount,
                                           @RequestHeader("X-Idempotency-Key") UUID idempotencyKey) {
        Optional<IdempotencyKey> existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            return switch (ik.getStatus()) {
                case COMPLETED -> ResponseEntity.ok().build();
                case FAILED -> ResponseEntity.status(ik.getHttpStatus()).build();
                case PROCESSING -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            };
        }
        idempotencyService.reserve(idempotencyKey, "updateBalance");
        try {
            BigDecimal updatedBalance = accountService.updateAccountBalance(login, currency, amount);
            idempotencyService.complete(idempotencyKey, 200);
            return ResponseEntity.ok(Map.of("balance", updatedBalance));
        } catch (Exception e) {
            idempotencyService.fail(idempotencyKey, 400);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<String>> getCurrencies(@PathVariable String login) {
        List<String> currencies = accountService.getCurrencies(login);
        return ResponseEntity.ok(currencies);
    }
}
