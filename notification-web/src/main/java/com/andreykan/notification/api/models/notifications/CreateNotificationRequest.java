package com.andreykan.notification.api.models.notifications;

public record CreateNotificationRequest(String idempotencyKey, String channel, String emailAddress, String title, String body) {
}
