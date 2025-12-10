package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

//@Service
//public class ExchangeClient {
//
//    private final RestTemplate restTemplate;
//    private final String exchangeServiceUrl;
//
//    public ExchangeClient(RestTemplate restTemplate,
//                          @Value("${EXCHANGE_SERVICE_URL}") String exchangeServiceUrl) {
//        this.restTemplate = restTemplate;
//        this.exchangeServiceUrl = exchangeServiceUrl;
//    }
//
//    public ConvertResponse convert(ConvertRequest request) {
//        String url = exchangeServiceUrl + "/api/exchange/convert";
//        return restTemplate.postForObject(url, request, ConvertResponse.class);
//    }
//}
import org.springframework.http.HttpRequest;
//import org.springframework.http.HttpResponse;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
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

        // Добавляем перехватчик для логирования заголовков запросов
        this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                // Логируем заголовки запроса (в том числе токен, если он есть)
                System.out.println("Request URL: " + request.getURI());
                System.out.println("Request Method: " + request.getMethod());
                System.out.println("Request Headers: " + request.getHeaders());

                // Выполняем запрос
                return execution.execute(request, body);
            }
        });
    }

    public ConvertResponse convert(ConvertRequest request) {
        String url = exchangeServiceUrl + "/api/exchange/convert";
        return restTemplate.postForObject(url, request, ConvertResponse.class);
    }
}
