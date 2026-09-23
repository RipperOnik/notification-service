package com.andreykan.notification.domain.entities;

import com.andreykan.notification.domain.valueobjects.notification.*;

import java.time.Instant;
import java.util.Set;

public class Notification {
    private NotificationId id;
    private NotificationStatus status;
    private NotificationChannel channel;
    private Recipient recipient;
    private NotificationTitle title;
    private NotificationBody body;
    private Set<DeliveryAttempt> deliveryAttempts;
    private Instant createdAt;
}
