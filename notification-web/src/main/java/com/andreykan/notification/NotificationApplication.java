package com.andreykan.notification;

import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.application.notification.createnotification.CreateNotificationUseCase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotificationApplication {

    static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
