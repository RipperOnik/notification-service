package com.andreykan.notification.schedulers;

import com.andreykan.notification.application.notification.dispatchnotifications.DispatchReadyNotificationsUseCase;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalNotificationScheduler {
    private static final Logger log =
            LoggerFactory.getLogger(LocalNotificationScheduler.class);

    private final DispatchReadyNotificationsUseCase useCase;

    public LocalNotificationScheduler(DispatchReadyNotificationsUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(fixedDelayString = "${dispatcher.interval-ms:5000}")
    public void dispatch() {
        log.debug("Starting notification dispatch cycle...");

        try {
            useCase.dispatchReadyNotifications();
            log.debug("Finished notification dispatch cycle");
        }
        catch (Exception ex){
            log.error(
                    "Notification dispatch cycle failed",
                    ex
            );
        }
    }
}
