package com.andreykan.notification.domain.valueobjects.notificationevent;

import java.util.Objects;
import java.util.UUID;

public record NotificationEventId(UUID value) {
    public NotificationEventId {
        Objects.requireNonNull(value, "Delivery Attempt id should not be null");
    }

    public static NotificationEventId generate() {
        return new NotificationEventId(UUID.randomUUID());
    }
}
