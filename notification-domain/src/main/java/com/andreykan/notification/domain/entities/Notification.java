package com.andreykan.notification.domain.entities;

import com.andreykan.notification.domain.valueobjects.notification.NotificationChannel;
import com.andreykan.notification.domain.valueobjects.notification.NotificationId;
import com.andreykan.notification.domain.valueobjects.notification.NotificationStatus;
import com.andreykan.notification.domain.valueobjects.notification.Recipient;
import java.util.Set;

public class Notification {
    private NotificationId id;
    private NotificationStatus status;
    private NotificationChannel channel;
    private Recipient recipient;
    private Set<DeliveryAttempt> deliveryAttempts;
}
