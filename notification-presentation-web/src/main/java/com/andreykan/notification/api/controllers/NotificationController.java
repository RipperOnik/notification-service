package com.andreykan.notification.api.controllers;

import com.andreykan.notification.api.models.notifications.CreateNotificationRequest;
import com.andreykan.notification.api.models.notifications.NotificationDto;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationCommand;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationResult;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationUseCase;
import com.andreykan.notification.application.notification.findbyid.FindNotificationByIdCommand;
import com.andreykan.notification.application.notification.findbyid.FindNotificationByIdUseCase;
import com.andreykan.notification.domain.notification.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(path = "/notifications")
public class NotificationController {
    private final CreateNotificationUseCase createNotificationUseCase;
    private final FindNotificationByIdUseCase findNotificationByIdUseCase;

    public NotificationController(CreateNotificationUseCase createNotificationUseCase, FindNotificationByIdUseCase findNotificationByIdUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
        this.findNotificationByIdUseCase = findNotificationByIdUseCase;
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

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDto> getNotification(@PathVariable UUID notificationId) {
        FindNotificationByIdCommand command = new FindNotificationByIdCommand(notificationId);
        Notification notification = findNotificationByIdUseCase.findById(command);

        return ResponseEntity.ok(new NotificationDto(notification.id().value()));
    }
}
