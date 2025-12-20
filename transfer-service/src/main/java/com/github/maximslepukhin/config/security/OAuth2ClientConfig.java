package com.github.maximslepukhin.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

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
                    .withClientRegistrationId("transfer-service")
                    .principal("transfer-service")
                    .build();

            var client = manager.authorize(authorizeRequest);
            if (client != null && client.getAccessToken() != null) {
                String token = client.getAccessToken().getTokenValue();
                System.out.println("Adding Bearer token to request: " + token);
                request.getHeaders().setBearerAuth(token);
            } else {
                System.out.println("No access token available for transfer-service");
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
