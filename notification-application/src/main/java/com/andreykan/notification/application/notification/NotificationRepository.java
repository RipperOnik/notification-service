package com.andreykan.notification.application.notification;

import com.andreykan.notification.domain.notification.IdempotencyKey;
import com.andreykan.notification.domain.notification.Notification;

import java.util.Optional;

public interface NotificationRepository {
    void save(Notification notification);
    Optional<Notification> findByIdempotencyKey(IdempotencyKey idempotencyKey);
}
