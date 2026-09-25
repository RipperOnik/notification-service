package com.andreykan.notification.domain.notification;

public record IdempotencyKey(String value) {
    private static final int MAX_LENGTH = 128;

    public IdempotencyKey {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Idempotency key cannot be empty");

        if(value.length() > MAX_LENGTH) throw new IllegalArgumentException(String.format("Idempotency key cannot exceed %d length", MAX_LENGTH));
    }
}
