package com.andreykan.notification.application.notification;

import com.andreykan.notification.application.notification.dispatchnotifications.DispatchNotificationCommand;

public interface NotificationPublisher {
    void publish(DispatchNotificationCommand command);
}
