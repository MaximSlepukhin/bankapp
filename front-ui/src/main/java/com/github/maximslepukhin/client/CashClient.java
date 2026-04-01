package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.CashOperationDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
public class CashClient {

    private final RestTemplate restTemplate;

    @Value("${CASH_SERVICE_URL:http://cash-service:8082}")
    private String cashServiceUrl;

    public CashClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "cashService", fallbackMethod = "depositFallback")
    public void deposit(CashOperationDto dto, UUID idempotencyKey) {
        String url = cashServiceUrl + "/api/v1/cash/deposit";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(dto, headers), Void.class);
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при депозите: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void depositFallback(CashOperationDto dto, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: cash-service недоступен при депозите: {}", t.getMessage());
        throw new RuntimeException("cash-service недоступен", t);
    }

    @CircuitBreaker(name = "cashService", fallbackMethod = "withdrawFallback")
    public void withdraw(CashOperationDto dto, UUID idempotencyKey) {
        String url = cashServiceUrl + "/api/v1/cash/withdraw";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(dto, headers), Void.class);
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при снятии: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void withdrawFallback(CashOperationDto dto, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: cash-service недоступен при снятии: {}", t.getMessage());
        throw new RuntimeException("cash-service недоступен", t);
    }
}
