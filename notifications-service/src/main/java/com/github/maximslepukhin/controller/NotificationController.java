package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.exception.NotificationNotFoundException;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NotificationRequest request) {
        Notification created = service.create(request);
        return ResponseEntity.ok(Map.of(
                "id", created.getId(),
                "login", created.getLogin(),
                "message", created.getMessage(),
                "createdAt", created.getCreatedAt()
        ));
    }

    @GetMapping("/{login}")
    public ResponseEntity<List<Notification>> getForUser(@PathVariable String login) {

        List<Notification> notifications = service.getForUser(login);
        if (notifications.isEmpty()) {
            throw new NotificationNotFoundException("No notifications for user: " + login);
        }
        return ResponseEntity.ok(notifications);
    }
}

