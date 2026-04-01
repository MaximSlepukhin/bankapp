package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.TransferRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
public class TransferClient {

    private final RestTemplate restTemplate;

    @Value("${TRANSFER_SERVICE_URL:http://transfer-service:8083}")
    private String transferServiceUrl;

    public TransferClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "transferService", fallbackMethod = "transferFallback")
    public void transfer(TransferRequestDto dto, UUID idempotencyKey) {
        String url = transferServiceUrl + "/api/v1/transfer";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey.toString());
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(dto, headers), Void.class);
            log.info("Запрос успешно выполнен");
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса на перевод", e);
            throw e;
        }
    }

    private void transferFallback(TransferRequestDto dto, UUID idempotencyKey, Throwable t) {
        log.error("Circuit breaker: transfer-service недоступен: {}", t.getMessage());
        throw new RuntimeException("transfer-service недоступен", t);
    }
}
