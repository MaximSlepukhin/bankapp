package com.github.maximslepukhin.config.rest;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    public RestTemplateConfig(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    public RestTemplate restTemplate() {
        return restTemplateBuilder
                .additionalInterceptors((request, body, execution) -> {
                    System.out.println("Запрос: " + request.getMethod() + " " + request.getURI());
                    var response = execution.execute(request, body);
                    System.out.println("Ответ: " + response.getStatusCode());
                    return response;
                })
                .build();
    }
}
