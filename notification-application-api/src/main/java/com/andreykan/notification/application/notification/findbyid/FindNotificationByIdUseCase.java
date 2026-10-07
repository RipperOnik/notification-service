package com.andreykan.notification.application.notification.findbyid;

import com.andreykan.notification.domain.notification.Notification;

import java.util.Optional;

public interface FindNotificationByIdUseCase {
    Notification findById(FindNotificationByIdCommand command);
}
