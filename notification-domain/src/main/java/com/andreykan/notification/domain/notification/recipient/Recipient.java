package com.andreykan.notification.domain.notification.recipient;

import java.util.Objects;

public record Recipient(Email email) {
    public Recipient {
        Objects.requireNonNull(email);
    }
}
