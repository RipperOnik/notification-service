package com.andreykan.notification.infrastructure.dynamodb.config;

import com.andreykan.notification.infrastructure.notification.NotificationTableItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;


@Configuration
public class DynamoDbConfiguration {

    public static final TableSchema<NotificationTableItem>
            NOTIFICATION_TABLE_SCHEMA =
            TableSchema.fromBean(NotificationTableItem.class);

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    @Bean
    public DynamoDbTable<NotificationTableItem> notificationTable(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.table-name}") String tableName
    ) {
        return enhancedClient.table(
                tableName,
                NOTIFICATION_TABLE_SCHEMA
        );
    }
}
