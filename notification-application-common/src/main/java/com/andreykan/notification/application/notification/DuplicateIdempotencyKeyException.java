package com.andreykan.notification.application.notification;

import com.andreykan.notification.domain.notification.NotificationId;

public class DuplicateIdempotencyKeyException
        extends RuntimeException {

    private final NotificationId existingNotificationId;

    public DuplicateIdempotencyKeyException(
            NotificationId existingNotificationId) {

        super(
                "Idempotency key already belongs to notification "
                        + existingNotificationId
        );

        this.existingNotificationId =
                existingNotificationId;
    }

    public NotificationId existingNotificationId() {
        return existingNotificationId;
    }
}
