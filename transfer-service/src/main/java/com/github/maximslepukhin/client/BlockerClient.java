package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.record.BlockerStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class BlockerClient {

    private final RestTemplate restTemplate;
    private final String blockerServiceUrl;

    public BlockerClient(RestTemplate restTemplate,
                         @Value("${BLOCKER_SERVICE_URL}") String blockerServiceUrl) {
        this.restTemplate = restTemplate;
        this.blockerServiceUrl = blockerServiceUrl;
    }

    @CircuitBreaker(name = "blockerService", fallbackMethod = "checkFallback")
    public BlockerStatus check(BlockerRequest request) {
        return restTemplate.postForObject(blockerServiceUrl + "/api/v1/blocker/check", request, BlockerStatus.class);
    }

    private BlockerStatus checkFallback(BlockerRequest request, Throwable t) {
        log.error("Circuit breaker: blocker-service недоступен для login={}: {}. Операция заблокирована.", request.getLogin(), t.getMessage());
        return new BlockerStatus(true, "blocker-service недоступен");
    }
}
