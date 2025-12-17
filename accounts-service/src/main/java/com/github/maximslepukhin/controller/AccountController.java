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
        return ResponseEntity.ok(accountService.getBalance(login, currency));
    }


    // 🟠 Списать средства (debit)
    @PostMapping("/{login}/{currency}/debit")
    public ResponseEntity<?> debit(
            @PathVariable String login,
            @PathVariable String currency,
            @RequestParam BigDecimal amount
    ) {
        accountService.debit(login, currency, amount);
        return ResponseEntity.ok().build();
    }

    // 🟢 Зачислить средства (credit)
    @PostMapping("/{login}/{currency}/deposit")
    public ResponseEntity<?> credit(
            @PathVariable String login,
            @PathVariable String currency,
            @RequestParam BigDecimal amount
    ) {
        accountService.credit(login, currency, amount);
        return ResponseEntity.ok().build();
    }

    // 🟡 Обновить баланс (универсальный метод)
    @PostMapping("/{login}/{currency}")
    public ResponseEntity<?> updateBalance(
            @PathVariable String login,
            @PathVariable String currency,
            @RequestBody BigDecimal amount
    ) {
        BigDecimal updatedBalance = accountService.updateAccountBalance(login, currency, amount);
        return ResponseEntity.ok(Map.of("balance", updatedBalance));
    }

    // 🧾 Получить список валют пользователя
    @GetMapping("/{login}/currencies")
    public ResponseEntity<List<String>> getCurrencies(@PathVariable String login) {
        List<String> currencies = accountService.getCurrencies(login);
        return ResponseEntity.ok(currencies);
    }
}
