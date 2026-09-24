package com.andreykan.notification.domain.exceptions;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
    public InvalidStateTransitionException() {
    }
}
