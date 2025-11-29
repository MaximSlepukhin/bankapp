package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExchangeClient {

    private final RestTemplate restTemplate;
    private final String exchangeServiceUrl;

    public ExchangeClient(RestTemplate restTemplate,
                          @Value("${services.exchange.url}") String exchangeServiceUrl) {
        this.restTemplate = restTemplate;
        this.exchangeServiceUrl = exchangeServiceUrl;
    }

    public ConvertResponse convert(ConvertRequest request) {
        String url = exchangeServiceUrl + "/api/exchange/convert";
        return restTemplate.postForObject(url, request, ConvertResponse.class);
    }
}
