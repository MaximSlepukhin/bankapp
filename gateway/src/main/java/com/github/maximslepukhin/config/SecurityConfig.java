package com.github.maximslepukhin.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter logIncomingRequests() {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var path = request.getURI().getPath();
            HttpMethod method = request.getMethod();
            var methodName = (method != null ? method.name() : "UNKNOWN");
            var authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            log.debug("➡️ Incoming request: [{} {}]", methodName, path);

            if (authHeader == null) {
                log.warn("⚠️ No Authorization header for request: {}", path);
            } else {
                log.debug("🔐 Authorization header (partial): {}...", maskToken(authHeader));
            }

            return chain.filter(exchange)
                    .doOnSuccess(done -> log.trace("✅ Request {} completed", path))
                    .doOnError(err -> log.error("❌ Request {} failed: {}", path, err.getMessage()));
        };
    }

    private String maskToken(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token.length() > 20) {
                return "Bearer " + token.substring(0, 10) + "..." + token.substring(token.length() - 10);
            }
        }
        return authHeader;
    }
}