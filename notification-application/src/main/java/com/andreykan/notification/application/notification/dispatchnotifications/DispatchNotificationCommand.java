package com.andreykan.notification.application.notification.dispatchnotifications;

import com.andreykan.notification.domain.notification.NotificationId;

public record DispatchNotificationCommand(NotificationId notificationId) {
    
}
