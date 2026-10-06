package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.domain.notification.*;
import com.andreykan.notification.domain.notification.delivery.*;
import com.andreykan.notification.domain.notification.recipient.Email;
import com.andreykan.notification.domain.notification.recipient.Recipient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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

        item.setItemType(NotificationItemType.NOTIFICATION.name());

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

        if (notification.queuedAt() != null) {
            item.setQueuedAt(
                    notification.queuedAt().toEpochMilli()
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

        item.setItemType(NotificationItemType.DELIVERY_ATTEMPT.name());

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

        item.setItemType(NotificationItemType.IDEMPOTENCY.name());

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

    public Notification toDomain(List<NotificationTableItem> items) {

        NotificationTableItem root = items.stream()
                .filter(item ->
                        NotificationItemType.NOTIFICATION.name().equals(item.getItemType()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Notification root item is missing"
                        )
                );

        List<DeliveryAttempt> attempts = items.stream()
                .filter(item ->
                        NotificationItemType.DELIVERY_ATTEMPT.name().equals(item.getItemType())
                )
                .sorted(
                        Comparator.comparingInt(
                                NotificationTableItem::getAttemptNumber
                        )
                )
                .map(this::toDeliveryAttempt)
                .toList();

        return Notification.rehydrate(
                new NotificationId(
                        UUID.fromString(root.getNotificationId())
                ),

                new IdempotencyKey(
                        root.getIdempotencyKey()
                ),

                new Recipient(
                        new Email(
                                root.getRecipientAddress()
                        )
                ),

                new NotificationTitle(
                        root.getSubject()
                ),

                new NotificationBody(
                        root.getPayload()
                ),

                NotificationChannel.valueOf(
                        root.getChannel()
                ),

                NotificationStatus.valueOf(
                        root.getStatus()
                ),

                Instant.ofEpochMilli(
                        root.getCreatedAt()
                ),

                toInstant(root.getReadyAt()),
                toInstant(root.getFailedAt()),
                toInstant(root.getDeliveredAt()),
                toInstant(root.getQueuedAt()),
                attempts
        );
    }

    private DeliveryAttempt toDeliveryAttempt(
            NotificationTableItem item
    ) {

        DeliveryFailure failure = null;

        if (item.getFailureType() != null) {
            failure = new DeliveryFailure(
                    DeliveryFailureType.valueOf(
                            item.getFailureType()
                    ),
                    item.getFailureReason()
            );
        }

        return DeliveryAttempt.rehydrate(
                new DeliveryAttemptNumber(item.getAttemptNumber()),

                DeliveryAttemptStatus.valueOf(
                        item.getStatus()
                ),

                Instant.ofEpochMilli(
                        item.getStartedAt()
                ),

                toInstant(item.getSucceededAt()),
                toInstant(item.getFailedAt()),

                failure,

                item.getProviderMessageId()
        );
    }

    private Instant toInstant(Long value) {
        return value == null
                ? null
                : Instant.ofEpochMilli(value);
    }
}