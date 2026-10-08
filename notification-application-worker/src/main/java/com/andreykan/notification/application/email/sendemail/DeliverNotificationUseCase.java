package com.andreykan.notification.application.email.sendemail;

public interface DeliverNotificationUseCase {
    void execute(DeliverNotificationCommand command);
}
