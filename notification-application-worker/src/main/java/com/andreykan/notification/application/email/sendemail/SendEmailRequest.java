package com.andreykan.notification.application.email.sendemail;

public record SendEmailRequest(String title, String body, String senderEmail, String recipientEmail) {

}
