package com.andreykan.notification.application.notification;

import com.andreykan.notification.domain.notification.Notification;

public interface NotificationRepository {
    void save(Notification notification);
}
