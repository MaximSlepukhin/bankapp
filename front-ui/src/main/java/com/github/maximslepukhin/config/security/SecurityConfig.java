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
                        System.out.println("Добавлен end_session_endpoint: " + endSessionUrl);
                    } else {
                        System.out.println("end_session_endpoint уже есть: " + newMetadata.get("end_session_endpoint"));
                    }

                    var field = details.getClass().getDeclaredField("configurationMetadata");
                    field.setAccessible(true);
                    field.set(details, java.util.Collections.unmodifiableMap(newMetadata));

                } catch (Exception e) {
                    System.out.println("Ошибка при установке end_session_endpoint: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Не найден clientRegistration с id=keycloak");
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
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();

                            System.out.println("OAuth2 Login failed: " + exception.getClass().getName());
                            System.out.println("Message: " + exception.getMessage());

                            response.sendRedirect("/oauth2/authorization/keycloak?error");
                        })
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
}