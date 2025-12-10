//package com.github.maximslepukhin.config.security;
//
//import feign.RequestInterceptor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
//
//@Slf4j
//@Configuration
//public class FeignOAuth2Config {
//
//    private final OAuth2AuthorizedClientManager serviceAuthorizedClientManager;
//
//    public FeignOAuth2Config(OAuth2AuthorizedClientManager serviceAuthorizedClientManager) {
//        this.serviceAuthorizedClientManager = serviceAuthorizedClientManager;
//    }
//
//    @Bean
//    public RequestInterceptor oauth2FeignRequestInterceptor() {
//        return requestTemplate -> {
//            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
//                    .withClientRegistrationId("keycloak-service")
//                    .principal("service-client")
//                    .build();
//
//            OAuth2AuthorizedClient client = serviceAuthorizedClientManager.authorize(authorizeRequest);
//
//            if (client == null || client.getAccessToken() == null) {
//                log.error("Не удалось получить access token из Keycloak для Feign-клиента");
//                throw new IllegalStateException("Не удалось получить сервисный access token из Keycloak");
//            }
//
//            String token = client.getAccessToken().getTokenValue();
//            log.info("Получен access token длиной {} символов", token.length());
//            log.debug("Первые 40 символов токена: {}...", token.substring(0, Math.min(40, token.length())));
//
//            requestTemplate.header("Authorization", "Bearer " + token);
//        };
//    }
//}