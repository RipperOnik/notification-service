package com.andreykan.notification.application.email.sendemail;

import com.andreykan.notification.application.email.sendemail.exceptions.SendEmailException;
import com.andreykan.notification.application.logging.LogExecution;
import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.domain.notification.Notification;
import com.andreykan.notification.domain.notification.NotificationId;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class DeliverNotificationHandler implements DeliverNotificationUseCase {
    private final NotificationRepository notificationRepository;
    private final EmailClient emailClient;
    private final Clock clock;

    public DeliverNotificationHandler(NotificationRepository notificationRepository, EmailClient emailClient, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.emailClient = emailClient;
        this.clock = clock;
    }

    @LogExecution("deliver-notification")
    @Override
    public void execute(DeliverNotificationCommand command) {
        Instant now = clock.instant();

        Notification notification = notificationRepository.findById(new NotificationId(command.notificationId())).orElseThrow();

        notification.startDeliveryAttempt(now);
        notificationRepository.save(notification);

        try {
            SendEmailRequest request =
                    new SendEmailRequest(notification.title().value(), notification.body().value(), "test@test.com", notification.recipient().email().value());
            SendEmailConfirmation confirmation = emailClient.send(request);
            notification.recordDeliverySuccess(confirmation.providerMessageId(), confirmation.deliveredAt());
        }
        catch (SendEmailException ex) {
            notification.recordDeliveryFailure(ex.failure(), ex.failureAt());
        }

        notificationRepository.save(notification);
    }
}
