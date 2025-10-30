package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 🟢 Получить баланс по валюте
    @GetMapping("/{login}/{currency}")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable String login,
            @PathVariable String currency
    ) {
        try {
            return ResponseEntity.ok(accountService.getBalance(login, currency));
        } catch (RuntimeException e) {
            log.warn("❌ Счёт для login={}, currency={} не найден", login, currency);
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Списать средства (debit)
    @PostMapping("/{login}/{currency}/debit")
    public ResponseEntity<?> debit(
            @PathVariable String login,
            @PathVariable String currency,
            @RequestParam BigDecimal amount
    ) {
        log.info("➡️ debit: login={}, currency={}, amount={}", login, currency, amount);
        try {
            accountService.debit(login, currency, amount);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("❌ Ошибка в debit: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 🟢 Зачислить средства (credit)
    @PostMapping("/{login}/{currency}/deposit")
    public ResponseEntity<?> credit(
            @PathVariable String login,
            @PathVariable String currency,
            @RequestParam BigDecimal amount
    ) {
        log.info("➡️ credit: login={}, currency={}, amount={}", login, currency, amount);
        try {
            accountService.credit(login, currency, amount);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("❌ Ошибка в credit: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 🟡 Обновить баланс (универсальный метод)
    @PostMapping("/{login}/{currency}")
    public ResponseEntity<?> updateBalance(
            @PathVariable String login,
            @PathVariable String currency,
            @RequestBody BigDecimal amount
    ) {
        log.info("➡️ updateBalance: login={}, currency={}, amount={}", login, currency, amount);
        try {
            BigDecimal updatedBalance = accountService.updateAccountBalance(login, currency, amount);
            return ResponseEntity.ok(Map.of("balance", updatedBalance));
        } catch (RuntimeException e) {
            log.error("❌ Ошибка в updateBalance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 🧾 Получить список валют пользователя
    @GetMapping("/{login}/currencies")
    public ResponseEntity<List<String>> getCurrencies(@PathVariable String login) {
        try {
            List<String> currencies = accountService.getCurrencies(login);
            return ResponseEntity.ok(currencies);
        } catch (RuntimeException e) {
            log.warn("❌ У пользователя login={} нет доступных валют", login);
            return ResponseEntity.notFound().build();
        }
    }
}
