package com.github.maximslepukhin.config.feign;

import com.github.maximslepukhin.config.security.KeycloakTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FeignConfig {

    private final KeycloakTokenService keycloakTokenService;

    public FeignConfig(KeycloakTokenService keycloakTokenService) {
        this.keycloakTokenService = keycloakTokenService;
    }

    @Bean(name = "restTemplateWithAuth")
    public RestTemplate restTemplateWithAuth() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = keycloakTokenService.getAccessToken();
            request.getHeaders().setBearerAuth(token);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}