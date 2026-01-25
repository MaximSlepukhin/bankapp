package com.github.maximslepukhin.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clients) {

        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var service = new InMemoryOAuth2AuthorizedClientService(clients);
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

    @Bean
    public RestTemplate restTemplate(OAuth2AuthorizedClientManager manager) {
        var restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            var authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak-service")
                    .principal("cash-service")
                    .build();

            var client = manager.authorize(authorizeRequest);
            if (client != null && client.getAccessToken() != null) {
                String token = client.getAccessToken().getTokenValue();
                log.info("Adding Bearer token to request: {}", token);

                request.getHeaders().setBearerAuth(token);
            } else {
                log.warn("No access token available for cash-service");
            }

            return execution.execute(request, body);
        });

        return restTemplate;
    }
}