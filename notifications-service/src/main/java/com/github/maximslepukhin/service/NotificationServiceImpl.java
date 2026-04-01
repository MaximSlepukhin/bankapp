package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public Notification create(NotificationRequest request, String messageId) {
        if (repository.existsByMessageId(messageId)) {
            log.info("Duplicate notification skipped: messageId={}", messageId);
            return null;
        }
        try {
            Notification n = Notification.builder()
                    .login(request.getLogin())
                    .message(request.getMessage())
                    .createdAt(OffsetDateTime.now())
                    .messageId(messageId)
                    .build();
            return repository.save(n);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate notification skipped (race condition): messageId={}", messageId);
            return null;
        }
    }
}