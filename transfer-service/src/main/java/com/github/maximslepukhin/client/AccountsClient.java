package com.github.maximslepukhin.client;

import com.github.maximslepukhin.config.security.FeignOAuth2Config;
import org.springframework.cloud.openfeign.FeignClient;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "accounts-service",
        configuration = FeignOAuth2Config.class
)
public interface AccountsClient {

    @GetMapping("/api/accounts/{login}/currencies")
    List<String> getCurrencies(@PathVariable("login") String login);

    @PostMapping("/api/accounts/{login}/{currency}/debit")
    void debit(@PathVariable("login") String login,
               @PathVariable("currency") String currency,
               @RequestParam BigDecimal amount);

    @PostMapping("/api/accounts/{login}/{currency}/deposit")
    void credit(@PathVariable("login") String login,
                @PathVariable("currency") String currency,
                @RequestParam BigDecimal amount);
}