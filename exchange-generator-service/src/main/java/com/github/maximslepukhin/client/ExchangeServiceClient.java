package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExchangeServiceClient {

    private final RestTemplate restTemplate;
    private final String exchangeServiceUrl;

    public ExchangeServiceClient(RestTemplate restTemplate,
                                 @Value("${EXCHANGE_SERVICE_URL}") String exchangeServiceUrl) {
        this.restTemplate = restTemplate;
        this.exchangeServiceUrl = exchangeServiceUrl;
    }

    public void sendRates(List<CurrencyRate> rates) {
        try {
            restTemplate.postForEntity(exchangeServiceUrl, rates, Void.class);
        } catch (Exception e) {
            System.err.println("Ошибка отправки курсов: " + e.getMessage());
        }
    }
}