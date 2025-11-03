package com.github.maximslepukhin.config.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloakWaiter {

    private static final Logger log = LoggerFactory.getLogger(KeycloakWaiter.class);

    @EventListener(ApplicationReadyEvent.class)
    public void waitForKeycloak() {
        String url = "http://keycloak:8080/realms/bank-realm/.well-known/openid-configuration";
        RestTemplate rest = new RestTemplate();

        for (int i = 1; i <= 10; i++) {
            try {
                rest.getForObject(url, String.class);
                log.info("✅ Keycloak доступен: {}", url);
                return;
            } catch (Exception e) {
                log.warn("⏳ Keycloak ещё не готов (попытка {}/10)...", i);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.error("Keycloak не ответил после 10 попыток. Возможно, он не запущен?");
    }
}