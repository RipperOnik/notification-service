package com.andreykan.notification.domain.valueobjects.notification;

public record EmailAddress(String email) {
    public EmailAddress {
        if(email == null || email.isBlank()) throw new IllegalArgumentException("Email should not be empty");

        if(!email.contains("@")) throw new IllegalArgumentException("Email should be valid");
    }
}
