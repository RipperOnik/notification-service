package com.andreykan.notification.domain.valueobjects.deliveryattempt;

import java.util.Objects;
import java.util.UUID;

public record DeliveryAttemptId(UUID value) {
    public DeliveryAttemptId {
        Objects.requireNonNull(value, "Delivery Attempt id should not be null");
    }

    public static DeliveryAttemptId generate() {
        return new DeliveryAttemptId(UUID.randomUUID());
    }
}
