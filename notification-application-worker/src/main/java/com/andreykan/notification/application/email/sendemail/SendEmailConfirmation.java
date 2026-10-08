package com.andreykan.notification.application.email.sendemail;

import java.time.Instant;

public record SendEmailConfirmation(String providerMessageId, Instant deliveredAt) {
}
