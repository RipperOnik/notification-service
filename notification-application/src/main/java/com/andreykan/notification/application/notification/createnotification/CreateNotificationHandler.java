package com.andreykan.notification.application.notification.createnotification;

import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.domain.notification.*;
import com.andreykan.notification.domain.notification.recipient.EmailAddress;
import com.andreykan.notification.domain.notification.recipient.Recipient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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

        var recipient = new Recipient(new EmailAddress(command.emailAddress()));
        var title = new NotificationTitle(command.title());
        var body = new NotificationBody(command.body());
        var createdAt = Instant.now();

        var notification = new Notification(id, idempotencyKey, channel, recipient, title, body, createdAt);

        try {
            notificationRepository.save(notification);
        }
        catch (DuplicateIdempotencyKeyException exception)
        {
            var existing = notificationRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();

            return new CreateNotificationResult(existing.id().value());
        }

        return new CreateNotificationResult(id.value());
    }
}
