package com.andreykan.notification.domain.notification;

import com.andreykan.notification.domain.notification.delivery.*;
import com.andreykan.notification.domain.notification.exceptions.InvalidStateTransitionException;
import com.andreykan.notification.domain.notification.recipient.Recipient;
import com.andreykan.notification.domain.notification.retry.RetryPolicy;

import java.time.Instant;
import java.util.*;
import java.time.Duration;
import java.util.stream.Collectors;

public class Notification {
    private final NotificationId id;
    private final IdempotencyKey idempotencyKey;
    private NotificationStatus status;
    private final NotificationChannel channel;
    private final Recipient recipient;
    private final NotificationTitle title;
    private final NotificationBody body;
    private Set<DeliveryAttempt> deliveryAttempts;
    private final RetryPolicy retryPolicy;
    private final Instant createdAt;
    private Instant deliveredAt;
    private Instant failedAt;
    private Instant readyAt;
    private Instant queuedAt;

    private static final int MAX_ATTEMPTS = 3;


    public Notification(
            NotificationId id,
            IdempotencyKey idempotencyKey,
            NotificationChannel channel,
            Recipient recipient,
            NotificationTitle title,
            NotificationBody body,
            Instant createdAt
    ) {
        Objects.requireNonNull(id, "id should not be null");
        Objects.requireNonNull(idempotencyKey, "IdempotencyKey should not be null");
        Objects.requireNonNull(recipient, "Recipient should not be null");
        Objects.requireNonNull(title, "Notification title should not be null");
        Objects.requireNonNull(body, "Notification body should not be null");
        Objects.requireNonNull(createdAt, "Notification createdAt should not be null");

        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.status = NotificationStatus.Created;
        this.channel = channel;
        this.recipient = recipient;
        this.title = title;
        this.body = body;
        this.deliveryAttempts = new HashSet<>();
        this.createdAt = createdAt;
        this.readyAt = createdAt;
        this.retryPolicy = new RetryPolicy(MAX_ATTEMPTS, Duration.ofSeconds(30), Duration.ofSeconds(240));
    }

    public Notification(
            NotificationId id,
            IdempotencyKey idempotencyKey,
            Recipient recipient,
            NotificationTitle title,
            NotificationBody body,
            NotificationChannel channel,
            NotificationStatus status,
            Instant createdAt,
            Instant readyAt,
            Instant failedAt,
            Instant deliveredAt,
            Instant queuedAt,
            List<DeliveryAttempt> deliveryAttempts
    ) {
        this(id, idempotencyKey, channel, recipient, title, body, createdAt);
        Objects.requireNonNull(status, "Notification status should not be null");
        Objects.requireNonNull(deliveryAttempts, "Notification deliveryAttempts should not be null");

        this.status = status;
        this.readyAt = readyAt;
        this.failedAt = failedAt;
        this.deliveredAt = deliveredAt;
        this.queuedAt = queuedAt;
        this.deliveryAttempts = new HashSet<>(deliveryAttempts);
    }

    public void recordQueued(Instant now) {
        if(status != NotificationStatus.Created && status != NotificationStatus.RetryPending)
            throw new InvalidStateTransitionException("Notification can only move to Queued status from Created and RetryPending");

        status = NotificationStatus.Queued;
        queuedAt = now;
    }

    public void startDeliveryAttempt(Instant now) {
        if(status != NotificationStatus.Queued)
            throw new InvalidStateTransitionException("Notification can only move to Processing status from Queued");

        status = NotificationStatus.Processing;

        var activeDelivery = getActiveDeliveryAttempt();

        if(activeDelivery.isPresent())
        {
            deliveryAttempts.add(new DeliveryAttempt(new DeliveryAttemptNumber(activeDelivery.get().number().value() + 1), now));
        }
        else
        {
            deliveryAttempts.add(new DeliveryAttempt(now));
        }
    }

    public void recordDeliverySuccess(String providerMessageId, Instant deliveredAt) {
        Objects.requireNonNull(providerMessageId, "Notification providerMessageId should not be null");
        Objects.requireNonNull(deliveredAt, "Notification deliveredAt should not be null");

        if(status != NotificationStatus.Processing)
            throw new InvalidStateTransitionException("Notification can only move to Delivered status from Processing");

        status = NotificationStatus.Delivered;
        this.deliveredAt = deliveredAt;

        var activeDelivery = getActiveDeliveryAttempt().orElseThrow();

        activeDelivery.markSucceeded(deliveredAt, providerMessageId);

        readyAt = null;
    }

    public void recordDeliveryFailure(DeliveryFailure failure, Instant failedAt) {
        Objects.requireNonNull(failure, "Notification failure should not be null");
        Objects.requireNonNull(failedAt, "Notification failedAt should not be null");

        if(status != NotificationStatus.Processing)
            throw new InvalidStateTransitionException("Notification can only move to Delivered status from Processing");

        var activeDelivery = getActiveDeliveryAttempt().orElseThrow();
        activeDelivery.markFailed(failure, failedAt);

        if(failure.type() == DeliveryFailureType.Permanent || !retryPolicy.canRetry(deliveryAttempts.size()))
        {
            status = NotificationStatus.Failed;
            this.failedAt = failedAt;
            readyAt = null;

            return;
        }

        Duration delay = retryPolicy.retryDelay(deliveryAttempts.size());
        status = NotificationStatus.RetryPending;
        readyAt = failedAt.plus(delay);
    }

    private Optional<DeliveryAttempt> getActiveDeliveryAttempt() {
        return deliveryAttempts.stream().max(Comparator.comparing(da -> da.number().value()));
    }

    public static Notification rehydrate(
            NotificationId id,
            IdempotencyKey idempotencyKey,
            Recipient recipient,
            NotificationTitle title,
            NotificationBody body,
            NotificationChannel channel,
            NotificationStatus status,
            Instant createdAt,
            Instant readyAt,
            Instant failedAt,
            Instant deliveredAt,
            Instant queuedAt,
            List<DeliveryAttempt> deliveryAttempts
    ) {
        return new Notification(
                id,
                idempotencyKey,
                recipient,
                title,
                body,
                channel,
                status,
                createdAt,
                readyAt,
                failedAt,
                deliveredAt,
                queuedAt,
                deliveryAttempts
        );
    }

    public NotificationId id() {
        return id;
    }

    public IdempotencyKey idempotencyKey() { return idempotencyKey; }

    public NotificationStatus status() {
        return status;
    }

    public NotificationChannel channel() {
        return channel;
    }

    public Recipient recipient() {
        return recipient;
    }

    public Instant failedAt() {
        return failedAt;
    }

    public Instant deliveredAt() {
        return deliveredAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Set<DeliveryAttempt> deliveryAttempts() {
        return deliveryAttempts;
    }

    public NotificationBody body() {
        return body;
    }

    public NotificationTitle title() {
        return title;
    }

    public Instant readyAt() {
        return readyAt;
    }

    public Instant queuedAt() { return queuedAt; }
}
