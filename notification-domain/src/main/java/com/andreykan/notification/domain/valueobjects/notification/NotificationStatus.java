package com.andreykan.notification.domain.valueobjects.notification;

public enum NotificationStatus {
    Created,
    Queued,
    Processing,
    RetryPending,
    Delivered,
    Failed
}
