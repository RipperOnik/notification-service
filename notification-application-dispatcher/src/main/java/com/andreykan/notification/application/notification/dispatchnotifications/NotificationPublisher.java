package com.andreykan.notification.application.notification.dispatchnotifications;

public interface NotificationPublisher {
    void publish(DispatchNotificationCommand command);
}
