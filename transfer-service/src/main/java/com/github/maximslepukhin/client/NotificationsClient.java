package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationsClient {

    private final RestTemplate restTemplate;
    private final String notificationsServiceUrl;

    public NotificationsClient(RestTemplate restTemplate,
                               @Value("${services.notifications.url}") String notificationsServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationsServiceUrl = notificationsServiceUrl;
    }

    public void notify(NotificationRequest request) {
        String url = notificationsServiceUrl + "/api/notifications";
        restTemplate.postForObject(url, request, Void.class);
    }
}
