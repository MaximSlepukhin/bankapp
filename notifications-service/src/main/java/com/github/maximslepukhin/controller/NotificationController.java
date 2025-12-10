package com.github.maximslepukhin.controller;

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
        log.info("Received request to create notification: {}", request);
        try {
            Notification created = service.create(request);
            log.info("Notification created with id: {}", created.getId());

            return ResponseEntity.ok(Map.of(
                    "id", created.getId(),
                    "login", created.getLogin(),
                    "message", created.getMessage(),
                    "createdAt", created.getCreatedAt()
            ));
        } catch (Exception e) {
            log.error("Error while creating notification: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error while creating notification: " + e.getMessage());
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<List<Notification>> getForUser(@PathVariable String login) {
        log.info("Received request to get notifications for user: {}", login);
        try {
            List<Notification> notifications = service.getForUser(login);
            log.info("Found {} notifications for user: {}", notifications.size(), login);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error while retrieving notifications for user {}: {}", login, e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
}

