package com.andreykan.notification.application.email.sendemail;

import java.util.UUID;

public record DeliverNotificationCommand(UUID notificationId){
}
