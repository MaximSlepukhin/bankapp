package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.NotificationRequest;
import com.github.maximslepukhin.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationsService notificationsService;

    @PostMapping
    public void send(@RequestBody NotificationRequest request) {
        notificationsService.sendNotification(request);
    }
}