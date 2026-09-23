package com.andreykan.notification.domain.valueobjects.notification;

public record NotificationBody(String value) {
    private static final int MAX_LENGTH = 10000;

    public NotificationBody {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("NotificationBody should not be empty");

        value = value.trim();

        if(value.length() > MAX_LENGTH) throw new IllegalArgumentException(String.format("NotificationBody length should not exceed %d", MAX_LENGTH));
    }
}
