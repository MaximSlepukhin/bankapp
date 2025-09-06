package com.github.maximslepukhin.scheduler;

import com.github.maximslepukhin.client.ExchangeServiceClient;
import com.github.maximslepukhin.service.ExchangeRateGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateGenerator generator;
    private final ExchangeServiceClient client;

    @Scheduled(fixedRate = 1000)
    public void generateAndSendRates() {
        var rates = generator.generateRates();
        client.sendRates(rates);
    }
}