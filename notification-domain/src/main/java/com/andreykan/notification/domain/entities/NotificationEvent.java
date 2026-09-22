package com.andreykan.notification.domain.entities;

import com.andreykan.notification.domain.valueobjects.notificationevent.NotificationEventId;
import com.andreykan.notification.domain.valueobjects.notificationevent.NotificationEventStatus;
import com.andreykan.notification.domain.valueobjects.notificationevent.NotificationEventType;

public class NotificationEvent {
    private NotificationEventId id;
    private NotificationEventType type;
    private NotificationEventStatus status;
}
