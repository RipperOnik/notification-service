package com.andreykan.notification.domain.notification;

import com.andreykan.notification.domain.notification.delivery.DeliveryAttempt;
import com.andreykan.notification.domain.notification.exceptions.InvalidStateTransitionException;
import com.andreykan.notification.domain.notification.delivery.DeliveryFailure;
import com.andreykan.notification.domain.notification.delivery.DeliveryFailureType;
import com.andreykan.notification.domain.notification.recipient.Recipient;
import com.andreykan.notification.domain.notification.retry.RetryPolicy;

import java.time.Instant;
import java.util.*;
import java.time.Duration;

public class Notification {
    private final NotificationId id;
    private NotificationStatus status;
    private final NotificationChannel channel;
    private final Recipient recipient;
    private final NotificationTitle title;
    private final NotificationBody body;
    private final Set<DeliveryAttempt> deliveryAttempts;
    private String providerMessageId;
    private final RetryPolicy retryPolicy;
    private final Instant createdAt;
    private Instant deliveredAt;
    private Instant failedAt;
    private Instant nextRetryAt;

    private static final int MAX_ATTEMPTS = 3;


    public Notification(NotificationChannel channel, Recipient recipient, NotificationTitle title, NotificationBody body) {
        Objects.requireNonNull(recipient, "Recipient should not be null");
        Objects.requireNonNull(title, "Notification title should not be null");
        Objects.requireNonNull(body, "Notification body should not be null");

        this.id = NotificationId.generate();
        this.status = NotificationStatus.Created;
        this.channel = channel;
        this.recipient = recipient;
        this.title = title;
        this.body = body;
        this.deliveryAttempts = new HashSet<>();
        this.createdAt = Instant.now();
        this.retryPolicy = new RetryPolicy(MAX_ATTEMPTS, Duration.ofSeconds(30), Duration.ofSeconds(240));
    }

    public void queue() {
        if(status != NotificationStatus.Created && status != NotificationStatus.RetryPending)
            throw new InvalidStateTransitionException("Notification can only move to Queued status from Created and RetryPending");

        status = NotificationStatus.Queued;
    }

    public void startDeliveryAttempt() {
        if(status != NotificationStatus.Queued)
            throw new InvalidStateTransitionException("Notification can only move to Processing status from Queued");

        status = NotificationStatus.Processing;

        var activeDelivery = getActiveDeliveryAttempt();

        if(activeDelivery.isPresent())
        {
            deliveryAttempts.add(new DeliveryAttempt(activeDelivery.get().number().value() + 1));
        }
        else
        {
            deliveryAttempts.add(new DeliveryAttempt());
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

        activeDelivery.markSucceeded(deliveredAt);
        this.providerMessageId = providerMessageId;

        nextRetryAt = null;
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
            nextRetryAt = null;

            return;
        }

        Duration delay = retryPolicy.retryDelay(deliveryAttempts.size());
        status = NotificationStatus.RetryPending;
        nextRetryAt = failedAt.plus(delay);
    }

    private Optional<DeliveryAttempt> getActiveDeliveryAttempt() {
        return deliveryAttempts.stream().max(Comparator.comparing(da -> da.number().value()));
    }

    public NotificationId id() {
        return id;
    }

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

    public String providerMessageId() {
        return providerMessageId;
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

    public Instant nextRetryAt() {
        return nextRetryAt;
    }
}
