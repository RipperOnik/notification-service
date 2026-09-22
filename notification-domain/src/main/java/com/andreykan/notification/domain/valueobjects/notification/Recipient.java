package com.andreykan.notification.domain.valueobjects.notification;

import java.util.Objects;

public record Recipient(EmailAddress emailAddress) {
    public Recipient {
        Objects.requireNonNull(emailAddress);
    }
}
