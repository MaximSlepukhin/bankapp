package com.github.maximslepukhin.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("accounts-service", r -> r.path("/accounts-service/**")
                        .filters(f -> f
                                .rewritePath("/accounts-service/(?<path>.*)", "/${path}")
                                .filter((exchange, chain) -> {
                                    String authHeader = exchange.getRequest()
                                            .getHeaders()
                                            .getFirst(HttpHeaders.AUTHORIZATION);

                                    if (authHeader != null) {
                                        exchange = exchange.mutate()
                                                .request(req -> req
                                                        .headers(h -> h.set(HttpHeaders.AUTHORIZATION, authHeader)))
                                                .build();
                                    }

                                    return chain.filter(exchange);
                                })
                        )
                        .uri("lb://ACCOUNTS-SERVICE")) // Eureka
                .build();
    }
}