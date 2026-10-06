package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.domain.notification.IdempotencyKey;
import com.andreykan.notification.domain.notification.NotificationId;

public final class NotificationTableKeys {

    private NotificationTableKeys() {}

    public static String notificationPk(
            NotificationId id) {

        return NotificationItemType.NOTIFICATION.name() + "#" + id.value();
    }

    public static String notificationSk() {
        return NotificationItemType.NOTIFICATION.name();
    }

    public static String attemptSk(int number) {
        return NotificationItemType.DELIVERY_ATTEMPT.name() + "#%06d".formatted(number);
    }

    public static String idempotencyPk(
            IdempotencyKey key) {

        return NotificationItemType.IDEMPOTENCY.name() + "#" + key.value();
    }

    public static String idempotencySk() {
        return NotificationItemType.IDEMPOTENCY.name();
    }
}
