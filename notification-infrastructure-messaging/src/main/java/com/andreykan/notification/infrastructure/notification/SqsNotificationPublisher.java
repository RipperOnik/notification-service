package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.application.notification.dispatchnotifications.DispatchNotificationCommand;
import com.andreykan.notification.application.notification.dispatchnotifications.NotificationDispatchFailureException;
import com.andreykan.notification.application.notification.dispatchnotifications.NotificationPublisher;
import com.andreykan.notification.infrastructure.sqs.properties.DispatchQueueProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class SqsNotificationPublisher implements NotificationPublisher {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final DispatchQueueProperties queueProperties;

    public SqsNotificationPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            DispatchQueueProperties queueProperties
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueProperties = queueProperties;
    }

    @Override
    public void publish(DispatchNotificationCommand command) {
        try {
            String body =
                    objectMapper.writeValueAsString(command);

            sqsClient.sendMessage(request ->
                    request
                            .queueUrl(queueProperties.url())
                            .messageBody(body)
            );
        }
        catch (JacksonException e) {
            throw new NotificationDispatchFailureException("Failed to serialize dispatch command", e);
        }
        catch (Exception e) {
            throw new NotificationDispatchFailureException("An unexpected notification dispatch failure", e);
        }
    }
}
