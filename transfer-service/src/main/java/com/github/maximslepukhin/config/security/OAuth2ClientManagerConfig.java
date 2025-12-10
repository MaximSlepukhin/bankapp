//package com.github.maximslepukhin.config.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.*;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.web.filter.CommonsRequestLoggingFilter;
//
//@Configuration
//public class OAuth2ClientManagerConfig {
//
//    @Bean
//    public CommonsRequestLoggingFilter logFilter() {
//        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
//        filter.setIncludeQueryString(true);
//        filter.setIncludePayload(true);
//        filter.setIncludeHeaders(false);
//        filter.setMaxPayloadLength(10000);
//        return filter;
//    }
//
//    @Bean
//    public OAuth2AuthorizedClientManager authorizedClientManager(
//            ClientRegistrationRepository clients,
//            OAuth2AuthorizedClientService authorizedClientService
//    ) {
//        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
//                .clientCredentials()
//                .build();
//
//        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
//                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, authorizedClientService);
//
//        manager.setAuthorizedClientProvider(provider);
//        return manager;
//    }
//}