package com.andreykan.notification.infrastructure.sqs.config;

import com.andreykan.notification.infrastructure.sqs.properties.DispatchQueueProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsQueueConfiguration {

    @Bean
    public DispatchQueueProperties deliveryQueueProperties(
            SqsClient sqsClient,
            @Value("${app.sqs.delivery-queue-name}")
            String queueName
    ) {
        String queueUrl =
                sqsClient.getQueueUrl(request ->
                        request.queueName(queueName)
                ).queueUrl();

        return new DispatchQueueProperties(
                queueName,
                queueUrl
        );
    }
}
