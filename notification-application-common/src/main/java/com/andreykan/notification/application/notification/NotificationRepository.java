package com.andreykan.notification.application.notification;

import com.andreykan.notification.domain.notification.Notification;
import com.andreykan.notification.domain.notification.NotificationId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(Notification notification);
    List<Notification> fetchReadyToDispatch(Instant now);
    Optional<Notification> findById(NotificationId notificationId);
}
