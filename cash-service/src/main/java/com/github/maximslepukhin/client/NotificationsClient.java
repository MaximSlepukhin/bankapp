package com.github.maximslepukhin.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final RestTemplate restTemplate;

    @Value("${clients.notifications-service-url}")
    private String notificationsServiceUrl;

    public void notify(String login, String message) {
        restTemplate.postForEntity(
                notificationsServiceUrl + "/api/notifications",
                Map.of(
                        "login", login,
                        "message", message
                ),
                Void.class
        );
    }
}
