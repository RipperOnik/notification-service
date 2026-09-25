package com.andreykan.notification.domain.notification.recipient;

public record EmailAddress(String email) {
    public EmailAddress {
        if(email == null || email.isBlank()) throw new IllegalArgumentException("Email should not be empty");

        if(!email.contains("@")) throw new IllegalArgumentException("Email should be valid");
    }
}
