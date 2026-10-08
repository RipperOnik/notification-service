package com.andreykan.notification.worker.listeners;

import com.andreykan.notification.application.email.sendemail.DeliverNotificationCommand;
import com.andreykan.notification.application.email.sendemail.DeliverNotificationUseCase;
import com.andreykan.notification.worker.models.DispatchNotificationMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryListener {
    private final DeliverNotificationUseCase useCase;

    public NotificationDeliveryListener(DeliverNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @SqsListener("${app.sqs.delivery-queue-name}")
    public void handle(DispatchNotificationMessage message) {
        useCase.execute(new DeliverNotificationCommand(message.notificationId()));
    }
}
