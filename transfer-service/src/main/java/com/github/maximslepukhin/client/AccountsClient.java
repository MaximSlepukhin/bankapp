package com.github.maximslepukhin.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
public class AccountsClient {

    private final RestTemplate restTemplate;
    private final String accountsServiceUrl;

    public AccountsClient(RestTemplate restTemplate,
                          @Value("${ACCOUNTS_SERVICE_URL}") String accountsServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountsServiceUrl = accountsServiceUrl;
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "getCurrenciesFallback")
    @Retry(name = "accountsService")
    public List<String> getCurrencies(String login) {
        String url = String.format("%s/api/v1/accounts/%s/currencies", accountsServiceUrl, login);
        String[] response = restTemplate.getForObject(url, String[].class);
        return response != null ? Arrays.asList(response) : List.of();
    }

    private List<String> getCurrenciesFallback(String login, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при запросе валют login={}: {}", login, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "debitFallback")
    @Retry(name = "accountsService")
    public void debit(String login, String currency, BigDecimal amount, UUID idempotencyKey) {
        String url = String.format("%s/api/v1/accounts/%s/%s/debit?amount=%s",
                accountsServiceUrl, login, currency, amount);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(null, headers), Void.class);
    }

    private void debitFallback(String login, String currency, BigDecimal amount, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при дебете login={} currency={}: {}", login, currency, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "creditFallback")
    @Retry(name = "accountsService")
    public void credit(String login, String currency, BigDecimal amount, UUID idempotencyKey) {
        String url = String.format("%s/api/v1/accounts/%s/%s/deposit?amount=%s",
                accountsServiceUrl, login, currency, amount);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(null, headers), Void.class);
    }

    private void creditFallback(String login, String currency, BigDecimal amount, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при кредите login={} currency={}: {}", login, currency, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "compensateFallback")
    @Retry(name = "accountsService")
    public void compensate(String login, String currency, BigDecimal amount, UUID idempotencyKey) {
        String url = String.format("%s/api/v1/accounts/%s/%s/deposit?amount=%s",
                accountsServiceUrl, login, currency, amount);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(null, headers), Void.class);
        log.info("Компенсация выполнена: login={}, currency={}, amount={}", login, currency, amount);
    }

    private void compensateFallback(String login, String currency, BigDecimal amount, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при компенсации login={} currency={}: {}", login, currency, t.getMessage());
        throw new RuntimeException("accounts-service недоступен при компенсации", t);
    }
}
