package com.andreykan.notification.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Profile("prod")
public class ProductionDynamoDbConfiguration {

    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${aws.region}") String region
    ) {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }
}
