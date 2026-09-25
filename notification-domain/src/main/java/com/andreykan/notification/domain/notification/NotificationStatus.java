package com.andreykan.notification.domain.notification;

public enum NotificationStatus {
    Created,
    Queued,
    Processing,
    RetryPending,
    Delivered,
    Failed
}
