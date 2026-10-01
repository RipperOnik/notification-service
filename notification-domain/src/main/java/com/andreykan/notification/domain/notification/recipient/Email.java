package com.andreykan.notification.domain.notification.recipient;

public record Email(String value) {
    public Email {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Email should not be empty");

        if(!value.contains("@")) throw new IllegalArgumentException("Email should be valid");
    }
}
