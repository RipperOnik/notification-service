package com.andreykan.notification.application.notification.createnotification;

public interface CreateNotificationUseCase {
    CreateNotificationResult execute(CreateNotificationCommand command);
}
