package com.github.maximslepukhin.client;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final RestTemplate restTemplate;

    public void notify(String login, String message) {
        restTemplate.postForEntity(
                "http://NOTIFICATIONS-SERVICE/api/notifications",
                Map.of(
                        "login", login,
                        "message", message
                ),
                Void.class
        );
    }
}
