package com.andreykan.notification.application.email.sendemail.exceptions;

import com.andreykan.notification.domain.notification.delivery.DeliveryFailure;

import java.time.Instant;

public class SendEmailException extends RuntimeException {
    private final DeliveryFailure failure;
    private final Instant failureAt;

    public SendEmailException(Exception ex, DeliveryFailure failure, Instant failureAt) {
        super(ex);
        this.failure = failure;
        this.failureAt = failureAt;
    }

    public DeliveryFailure failure() {
        return failure;
    }

    public Instant failureAt() {
        return failureAt;
    }
}
