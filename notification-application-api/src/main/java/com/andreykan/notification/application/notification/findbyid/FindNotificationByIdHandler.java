package com.andreykan.notification.application.notification.findbyid;

import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.domain.notification.Notification;
import com.andreykan.notification.domain.notification.NotificationId;
import org.springframework.stereotype.Service;


@Service
public class FindNotificationByIdHandler implements FindNotificationByIdUseCase{
    private final NotificationRepository notificationRepository;

    public FindNotificationByIdHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification findById(FindNotificationByIdCommand command) {
        var notificationId = new NotificationId(command.id());

        return notificationRepository.findById(notificationId).orElseThrow();
    }
}
