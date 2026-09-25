package com.andreykan.notification.domain.notification.recipient;

import java.util.Objects;

public record Recipient(EmailAddress emailAddress) {
    public Recipient {
        Objects.requireNonNull(emailAddress);
    }
}
