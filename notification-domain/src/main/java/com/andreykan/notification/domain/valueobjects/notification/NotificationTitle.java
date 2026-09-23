package com.andreykan.notification.domain.valueobjects.notification;

public record NotificationTitle(String value) {
    private static final int MAX_LENGTH = 200;

    public NotificationTitle {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("NotificationTitle should not be empty");

        value = value.trim();

        if(value.length() > MAX_LENGTH) throw new IllegalArgumentException(String.format("NotificationTitle length should not exceed %d", MAX_LENGTH));
    }
}
