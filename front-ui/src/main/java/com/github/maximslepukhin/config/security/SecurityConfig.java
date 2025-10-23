package com.github.maximslepukhin.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${keycloak.server-url:http://localhost:8082}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:bank-realm}")
    private String realm;

    @Bean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(
            ClientRegistrationRepository clientRegistrationRepository) {

        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        handler.setPostLogoutRedirectUri("{baseUrl}/logout-success");

        System.out.println("=== Проверка clientRegistrationRepository ===");

        if (clientRegistrationRepository instanceof InMemoryClientRegistrationRepository repo) {
            repo.iterator().forEachRemaining(reg -> {
                System.out.println("➡️ Найден client: " + reg.getRegistrationId());
                System.out.println("   provider: " + reg.getProviderDetails().getAuthorizationUri());
            });

            var registration = repo.findByRegistrationId("keycloak");
            if (registration != null) {
                try {
                    var details = registration.getProviderDetails();
                    var oldMetadata = details.getConfigurationMetadata();

                    var newMetadata = new java.util.HashMap<>(oldMetadata);

                    if (!newMetadata.containsKey("end_session_endpoint")) {
                        String endSessionUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
                        newMetadata.put("end_session_endpoint", endSessionUrl);
                        System.out.println("✅ Добавлен end_session_endpoint: " + endSessionUrl);
                    } else {
                        System.out.println("ℹ️ end_session_endpoint уже есть: " + newMetadata.get("end_session_endpoint"));
                    }

                    var field = details.getClass().getDeclaredField("configurationMetadata");
                    field.setAccessible(true);
                    field.set(details, java.util.Collections.unmodifiableMap(newMetadata));

                } catch (Exception e) {
                    System.out.println("⚠️ Ошибка при установке end_session_endpoint: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Не найден clientRegistration с id=keycloak");
            }
        }

        return handler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signup",
                                "/css/**",
                                "/js/**",
                                "/login/oauth2/**",
                                "/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .defaultSuccessUrl("/main", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public OAuth2AuthorizedClientManager serviceAuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }
}