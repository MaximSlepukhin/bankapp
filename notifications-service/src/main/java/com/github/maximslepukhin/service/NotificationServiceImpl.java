package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public Notification create(NotificationRequest request) {
        Notification n = Notification.builder()
                .login(request.getLogin())
                .message(request.getMessage())
                .createdAt(OffsetDateTime.now())
                .build();
        return repository.save(n);
    }
}