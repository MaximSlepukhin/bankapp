package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ExchangeClient {

    private final RestTemplate restTemplate;

    @Value("${EXCHANGE_SERVICE_URL:http://exchange-generator-service:8080}")
    private String exchangeServiceUrl;

    public ExchangeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "exchangeService", fallbackMethod = "getRatesFallback")
    public List<CurrencyRate> getRates() {
        String url = exchangeServiceUrl + "/api/v1/rates";
        CurrencyRate[] rates = restTemplate.getForObject(url, CurrencyRate[].class);
        if (rates != null && rates.length > 0) {
            log.info("Received {} currency rates: {}", rates.length, Arrays.toString(rates));
        } else {
            log.warn("No rates received or response is empty.");
        }
        return rates != null ? Arrays.asList(rates) : List.of();
    }

    private List<CurrencyRate> getRatesFallback(Throwable t) {
        log.error("Circuit breaker: exchange-service недоступен: {}", t.getMessage());
        return List.of();
    }
}
