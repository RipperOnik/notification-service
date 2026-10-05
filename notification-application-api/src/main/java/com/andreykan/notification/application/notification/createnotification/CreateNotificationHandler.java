package com.andreykan.notification.application.notification.createnotification;

import com.andreykan.notification.application.notification.DuplicateIdempotencyKeyException;
import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.domain.notification.*;
import com.andreykan.notification.domain.notification.recipient.Email;
import com.andreykan.notification.domain.notification.recipient.Recipient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
class CreateNotificationHandler implements CreateNotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final Map<String, NotificationChannel> notificationChannelMap;

    public CreateNotificationHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        notificationChannelMap = new HashMap<>(Map.of("email", NotificationChannel.Email));
    }

    @Override
    public CreateNotificationResult execute(CreateNotificationCommand command) {
        var id = NotificationId.generate();
        var idempotencyKey = new IdempotencyKey(command.idempotencyKey());

        if(!notificationChannelMap.containsKey(command.channel())) throw new IllegalArgumentException("Channel is invalid");
        var channel = notificationChannelMap.get(command.channel());

        var recipient = new Recipient(new Email(command.emailAddress()));
        var title = new NotificationTitle(command.title());
        var body = new NotificationBody(command.body());
        var createdAt = Instant.now();

        var notification = new Notification(id, idempotencyKey, channel, recipient, title, body, createdAt);

        try {
            notificationRepository.save(notification);
        }
        catch (DuplicateIdempotencyKeyException exception)
        {
            return new CreateNotificationResult(exception.existingNotificationId().value());
        }

        return new CreateNotificationResult(id.value());
    }
}
