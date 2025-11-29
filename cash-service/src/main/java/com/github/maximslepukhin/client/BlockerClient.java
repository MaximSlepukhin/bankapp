package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.record.BlockerRequest;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.model.enums.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BlockerClient {

    private final RestTemplate restTemplate;

    @Value("${clients.blocker-service-url}")
    private String blockerServiceUrl;

    public boolean isBlocked(String login, Currency currency, BigDecimal amount) {
        BlockerRequest request = new BlockerRequest(login, currency.name(), amount);

        BlockerStatus response = restTemplate.postForObject(
                blockerServiceUrl + "/api/blocker/check",
                request,
                BlockerStatus.class
        );

        return response != null && response.blocked();
    }
}
