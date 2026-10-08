package com.andreykan.notification.application.email.sendemail;

public record SendEmailRequest(String subject, String body, String to) {

}
