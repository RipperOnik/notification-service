package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.domain.notification.IdempotencyKey;
import com.andreykan.notification.domain.notification.NotificationId;

public final class NotificationTableKeys {

    private NotificationTableKeys() {}

    public static String notificationPk(
            NotificationId id) {

        return "NOTIFICATION#" + id.value();
    }

    public static String notificationSk() {
        return "NOTIFICATION";
    }

    public static String attemptSk(int number) {
        return "ATTEMPT#%06d".formatted(number);
    }

    public static String idempotencyPk(
            IdempotencyKey key) {

        return "IDEMPOTENCY#" + key.value();
    }

    public static String idempotencySk() {
        return "IDEMPOTENCY";
    }
}
