package com.andreykan.notification.schedulers;

import com.andreykan.notification.application.notification.dispatchnotifications.DispatchReadyNotificationsUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalNotificationScheduler {
    private final DispatchReadyNotificationsUseCase useCase;

    public LocalNotificationScheduler(DispatchReadyNotificationsUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(fixedDelayString = "${dispatcher.interval-ms:5000}")
    public void dispatch() {
        useCase.dispatchReadyNotifications();
    }
}
