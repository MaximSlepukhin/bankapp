package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.record.BlockerRequest;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.model.enums.Currency;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerClient {

    private final RestTemplate restTemplate;

    @Value("${clients.blocker-service-url}")
    private String blockerServiceUrl;

    @CircuitBreaker(name = "blockerService", fallbackMethod = "isBlockedFallback")
    public boolean isBlocked(String login, Currency currency, BigDecimal amount) {
        BlockerRequest request = new BlockerRequest(login, currency.name(), amount);

        BlockerStatus response = restTemplate.postForObject(
                blockerServiceUrl + "/api/v1/blocker/check",
                request,
                BlockerStatus.class
        );

        return response != null && response.blocked();
    }

    private boolean isBlockedFallback(String login, Currency currency, BigDecimal amount, Throwable t) {
        log.error("Circuit breaker: blocker-service недоступен для login={}: {}. Операция заблокирована.", login, t.getMessage());
        return true;
    }
}
