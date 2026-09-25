package com.andreykan.notification.domain.notification.exceptions;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
    public InvalidStateTransitionException() {
    }
}
