package com.andreykan.notification.application.email.sendemail;

public interface EmailClient {
    SendEmailConfirmation send(SendEmailRequest request);
}
