package com.github.maximslepukhin.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FeignErrorDecoderConfig {

    @Bean(name = "restTemplateWithErrorHandler")
    public RestTemplate restTemplateWithErrorHandler(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new CustomErrorHandler(objectMapper));
        return restTemplate;
    }

    public static class CustomErrorHandler implements ResponseErrorHandler {

        private final ObjectMapper objectMapper;

        public CustomErrorHandler(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
        }

        @Override
        public void handleError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            HttpHeaders headers = response.getHeaders();

            throw new HttpClientErrorException(
                    response.getStatusCode(),
                    response.getStatusText(),
                    headers,
                    body != null ? body.getBytes(StandardCharsets.UTF_8) : null,
                    StandardCharsets.UTF_8
            );
        }
    }
}