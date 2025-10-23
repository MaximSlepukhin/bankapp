package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExchangeServiceClient {

    private final RestTemplate restTemplate;

    public ExchangeServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendRates(List<CurrencyRate> rates) {
        try {
            restTemplate.postForEntity(
                    "http://EXCHANGE-SERVICE/api/exchange/rates", // ✅ имя сервиса из Eureka
                    rates,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Ошибка отправки курсов: " + e.getMessage());
        }
    }
}