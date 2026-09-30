package com.andreykan.notification.api.controllers;

import com.andreykan.notification.api.models.notifications.CreateNotificationRequest;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationCommand;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationResult;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(path = "/notifications")
public class NotificationController {
    private final CreateNotificationUseCase createNotificationUseCase;

    public NotificationController(CreateNotificationUseCase createNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
    }

    @PostMapping
    public ResponseEntity<String> createNotification(@RequestBody CreateNotificationRequest request) {

        CreateNotificationCommand command = new CreateNotificationCommand(request.idempotencyKey(), request.channel(), request.emailAddress(), request.title(), request.body());

        CreateNotificationResult result = createNotificationUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.notificationId())
                .toUri();

        return ResponseEntity.created(location).body(result.notificationId().toString());
    }
}
