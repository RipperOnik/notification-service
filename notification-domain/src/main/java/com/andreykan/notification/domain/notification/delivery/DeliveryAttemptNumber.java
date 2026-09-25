package com.andreykan.notification.domain.notification.delivery;

public record DeliveryAttemptNumber(int value) {
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 3;

    public DeliveryAttemptNumber {
        if(value < MIN_VALUE || value > MAX_VALUE) throw new IllegalArgumentException(String.format("DeliveryAttemptNumber must be within %d and %d", MIN_VALUE, MAX_VALUE));
    }

    public DeliveryAttemptNumber() {
        this(1);
    }
}
