package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.domain.notification.Notification;
import com.andreykan.notification.domain.notification.NotificationId;
import com.andreykan.notification.domain.notification.delivery.DeliveryAttempt;
import org.springframework.stereotype.Component;

@Component
public class NotificationItemMapper {

    public NotificationTableItem toNotificationItem(
            Notification notification) {

        var item = new NotificationTableItem();

        item.setPk(
                NotificationTableKeys.notificationPk(
                        notification.id()
                )
        );

        item.setSk(
                NotificationTableKeys.notificationSk()
        );

        item.setItemType("NOTIFICATION");

        item.setNotificationId(
                notification.id().value().toString()
        );

        item.setIdempotencyKey(
                notification.idempotencyKey().value()
        );

        item.setStatus(
                notification.status().name()
        );

        item.setRecipientAddress(
                notification.recipient()
                        .email()
                        .value()
        );

        item.setSubject(
                notification.title().value()
        );

        item.setPayload(
                notification.body().value()
        );

        item.setChannel(
                notification.channel().name()
        );

        item.setCreatedAt(
                notification.createdAt().toEpochMilli()
        );

        if (notification.readyAt() != null) {
            item.setReadyAt(
                    notification.readyAt().toEpochMilli()
            );
        }

        if (notification.failedAt() != null) {
            item.setFailedAt(
                    notification.failedAt().toEpochMilli()
            );
        }

        if (notification.deliveredAt() != null) {
            item.setDeliveredAt(
                    notification.deliveredAt().toEpochMilli()
            );
        }

        return item;
    }

    public NotificationTableItem toAttemptItem(
            NotificationId notificationId,
            DeliveryAttempt attempt) {

        var item = new NotificationTableItem();

        item.setPk(
                NotificationTableKeys.notificationPk(
                        notificationId
                )
        );

        item.setSk(
                NotificationTableKeys.attemptSk(
                        attempt.number().value()
                )
        );

        item.setItemType("DELIVERY_ATTEMPT");

        item.setNotificationId(
                notificationId.value().toString()
        );

        item.setAttemptNumber(
                attempt.number().value()
        );

        item.setStatus(
                attempt.status().name()
        );

        item.setStartedAt(
                attempt.startedAt().toEpochMilli()
        );

        if (attempt.succeededAt() != null) {
            item.setSucceededAt(
                    attempt.succeededAt().toEpochMilli()
            );
        }

        if (attempt.failedAt() != null) {
            item.setFailedAt(
                    attempt.failedAt().toEpochMilli()
            );
        }

        if (attempt.failure() != null) {
            item.setFailureType(
                    attempt.failure().type().name()
            );

            item.setFailureReason(
                    attempt.failure().reason()
            );
        }

        if (attempt.providerMessageId() != null) {
            item.setProviderMessageId(
                    attempt.providerMessageId()
            );
        }

        return item;
    }

    public NotificationTableItem toIdempotencyItem(
            Notification notification) {

        var item = new NotificationTableItem();

        item.setPk(
                NotificationTableKeys.idempotencyPk(
                        notification.idempotencyKey()
                )
        );

        item.setSk(
                NotificationTableKeys.idempotencySk()
        );

        item.setItemType("IDEMPOTENCY");

        item.setNotificationId(
                notification.id().value().toString()
        );

        item.setIdempotencyKey(
                notification.idempotencyKey().value()
        );

        item.setCreatedAt(
                notification.createdAt().toEpochMilli()
        );

        return item;
    }
}