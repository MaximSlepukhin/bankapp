package com.github.maximslepukhin.config.feign;

import com.github.maximslepukhin.config.security.KeycloakTokenService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private final KeycloakTokenService keycloakTokenService;

    public FeignConfig(KeycloakTokenService keycloakTokenService) {
        this.keycloakTokenService = keycloakTokenService;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = keycloakTokenService.getAccessToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}

