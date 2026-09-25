package com.andreykan.notification.application.notification.createnotification;

public record CreateNotificationCommand(String idempotencyKey, String channel, String emailAddress, String title, String body) {
}
