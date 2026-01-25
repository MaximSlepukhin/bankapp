package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.io.IOException;

@Service
public class ExchangeClient {

    private final RestTemplate restTemplate;
    private final String exchangeServiceUrl;

    public ExchangeClient(RestTemplate restTemplate,
                          @Value("${EXCHANGE_SERVICE_URL}") String exchangeServiceUrl) {
        this.restTemplate = restTemplate;
        this.exchangeServiceUrl = exchangeServiceUrl;
        this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                return execution.execute(request, body);
            }
        });
    }

    public ConvertResponse convert(ConvertRequest request) {
        String url = exchangeServiceUrl + "/api/exchange/convert";
        return restTemplate.postForObject(url, request, ConvertResponse.class);
    }
}
