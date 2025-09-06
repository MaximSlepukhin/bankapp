package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.CurrencyRate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExchangeServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String exchangeServiceUrl = "http://exchange-service:8085/api/exchange/rates";

    public void sendRates(List<CurrencyRate> rates) {
        try {
            restTemplate.postForEntity(exchangeServiceUrl, rates, Void.class);
        } catch (Exception e) {
            System.err.println("Ошибка отправки курсов: " + e.getMessage());
        }
    }
}