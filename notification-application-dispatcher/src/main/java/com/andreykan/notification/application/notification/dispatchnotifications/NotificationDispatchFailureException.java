package com.andreykan.notification.application.notification.dispatchnotifications;

public class NotificationDispatchFailureException extends RuntimeException {
    public  NotificationDispatchFailureException(String message, Exception inner) {
        super(message, inner);
    }
}
