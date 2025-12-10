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

        // Устанавливаем клиентскую авторизацию для client_credentials потока
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()  // Используем client_credentials flow
                .build();

        // Создаем сервис для хранения авторизованных клиентов
        var service = new InMemoryOAuth2AuthorizedClientService(clients);

        // Создаем и настраиваем менеджер авторизованных клиентов
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

    @Bean
    public RestTemplate restTemplate(OAuth2AuthorizedClientManager manager) {
        // Создаем RestTemplate с перехватчиком для добавления токена в заголовок
        var restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Создаем запрос для авторизации с использованием client_id и client_secret
            var authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("transfer-service")  // Указываем client-id из Keycloak
                    .principal("transfer-service")  // Может быть любое имя principal, соответствующее конфигурации
                    .build();

            // Получаем токен
            var client = manager.authorize(authorizeRequest);
            if (client != null && client.getAccessToken() != null) {
                String token = client.getAccessToken().getTokenValue();
                // Логируем токен для отладки
                System.out.println("Adding Bearer token to request: " + token);
                // Добавляем токен в заголовки запроса
                request.getHeaders().setBearerAuth(token);
            } else {
                System.out.println("No access token available for transfer-service");
            }

            // Выполняем запрос с добавленным токеном
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
