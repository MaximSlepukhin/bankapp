package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.enums.Currency;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class AccountsClient {

    private final RestTemplate restTemplate;

    @Value("${clients.accounts-service-url}")
    private String accountsServiceUrl;

    public AccountsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "getBalanceFallback")
    @Retry(name = "accountsService")
    public BigDecimal getBalance(String login, Currency currency) {
        return restTemplate.getForObject(
                accountsServiceUrl + "/api/v1/accounts/{login}/{currency}",
                BigDecimal.class,
                login,
                currency.name()
        );
    }

    private BigDecimal getBalanceFallback(String login, Currency currency, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при запросе баланса login={} currency={}: {}", login, currency, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "accountsService")
    public void updateBalance(String login, Currency currency, BigDecimal amount, UUID idempotencyKey) {
        String url = accountsServiceUrl + "/api/v1/accounts/" + login + "/" + currency.name();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        headers.set("Content-Type", "application/json");
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(amount, headers), Void.class);
    }

    private void updateBalanceFallback(String login, Currency currency, BigDecimal amount, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при обновлении баланса login={} currency={}: {}", login, currency, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }
}
