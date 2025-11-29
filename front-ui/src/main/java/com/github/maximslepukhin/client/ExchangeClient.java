package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class ExchangeClient {

    private final RestTemplate restTemplate;

    public ExchangeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<CurrencyRate> getRates() {
        String url = "${EXCHANGE_SERVICE_URL:http://exchange-service:8084}/api/rates";
        CurrencyRate[] rates = restTemplate.getForObject(url, CurrencyRate[].class);
        return rates != null ? Arrays.asList(rates) : List.of();
    }
}
