package com.github.maximslepukhin.client;


import com.github.maximslepukhin.model.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications-service")
public interface NotificationsClient {

    @PostMapping("/api/notifications")
    void notify(@RequestBody NotificationRequest request);
}

