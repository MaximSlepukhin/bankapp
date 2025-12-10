package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final RestTemplate restTemplate;

    @Value("${clients.notifications-service-url}")
    private String notificationsServiceUrl;

    public void notify(String login, String message) {
        log.info("Preparing to send notification to user: {}, message: {}", login, message);

        try {
            NotificationRequest request = new NotificationRequest(login, message);

            String url = notificationsServiceUrl + "/api/notifications";
            log.info("POST URL: {}", url);
            log.info("Request body: {}", request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Notification response status: {}", response.getStatusCode());
            log.info("Notification response body: {}", response.getBody());

            log.info("Notification successfully sent to user: {}", login);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error while sending notification to user: {}, message: {}. Status: {}, Body: {}",
                    login, message, e.getStatusCode(), e.getResponseBodyAsString(), e);

        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException: Could not reach notifications service for user: {}, message: {}. Cause: {}",
                    login, message, e.getMessage(), e);

        } catch (Exception e) {
            log.error("Unexpected error while sending notification to user: {}, message: {}", login, message, e);
        }
    }
}