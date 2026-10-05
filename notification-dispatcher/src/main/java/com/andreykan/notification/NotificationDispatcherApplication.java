package com.andreykan.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationDispatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(
                NotificationDispatcherApplication.class,
                args
        );
    }
}
