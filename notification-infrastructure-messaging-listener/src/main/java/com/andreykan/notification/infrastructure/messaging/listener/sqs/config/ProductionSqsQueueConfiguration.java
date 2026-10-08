package com.andreykan.notification.infrastructure.messaging.listener.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@Profile("prod")
public class ProductionSqsQueueConfiguration {

    @Bean
    public SqsAsyncClient sqsAsyncClient(
            @Value("${aws.region}") String region
    ) {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .build();
    }
}
