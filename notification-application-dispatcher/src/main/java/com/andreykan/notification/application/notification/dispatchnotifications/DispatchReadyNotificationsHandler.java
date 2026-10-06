package com.andreykan.notification.application.notification.dispatchnotifications;

import com.andreykan.notification.application.logging.LogExecution;
import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.domain.notification.Notification;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class DispatchReadyNotificationsHandler implements DispatchReadyNotificationsUseCase{
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;
    private final Clock clock;

    public DispatchReadyNotificationsHandler(NotificationRepository notificationRepository, NotificationPublisher notificationPublisher, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.notificationPublisher = notificationPublisher;
        this.clock = clock;
    }

    @Override
    @LogExecution("dispatch-ready-notifications")
    public void dispatchReadyNotifications() {
        Instant now = clock.instant();
        var readyNotifications = notificationRepository.fetchReadyToDispatch(now);

        for (Notification notification: readyNotifications) {
            notificationPublisher.publish(new DispatchNotificationCommand(notification.id()));

            notification.recordQueued(now);

            notificationRepository.save(notification);
        }
    }
}
