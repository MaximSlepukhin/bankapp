package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

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
        Notification saved = repository.save(n);
        return saved;
    }

    @Override
    public List<Notification> getForUser(String login) {
        return repository.findByLoginOrderByCreatedAtDesc(login);
    }
}
