package com.andreykan.notification.domain.notification;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID value) {
    public NotificationId {
        Objects.requireNonNull(value, "Notification id should not be null");
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }
}
