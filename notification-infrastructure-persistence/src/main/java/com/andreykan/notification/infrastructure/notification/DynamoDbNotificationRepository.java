package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.application.notification.DuplicateIdempotencyKeyException;
import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.domain.notification.IdempotencyKey;
import com.andreykan.notification.domain.notification.Notification;
import com.andreykan.notification.domain.notification.NotificationId;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DynamoDbNotificationRepository implements NotificationRepository {
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<NotificationTableItem> table;
    private final NotificationItemMapper mapper;

    public DynamoDbNotificationRepository(
            DynamoDbEnhancedClient enhancedClient,
            DynamoDbTable<NotificationTableItem> table,
            NotificationItemMapper mapper) {

        this.enhancedClient = enhancedClient;
        this.table = table;
        this.mapper = mapper;
    }


    @Override
    public void save(Notification notification) {
        var rootItem =
                mapper.toNotificationItem(notification);

        var idempotencyItem =
                mapper.toIdempotencyItem(notification);

        List<NotificationTableItem> attemptItems =
                notification.deliveryAttempts()
                        .stream()
                        .map(attempt ->
                                mapper.toAttemptItem(
                                        notification.id(),
                                        attempt
                                )
                        )
                        .toList();

        Expression idempotencyCondition =
                idempotencyCondition(notification);

        try {
            enhancedClient.transactWriteItems(transaction -> {

                // 1. Save aggregate root.
                transaction.addPutItem(
                        table,
                        rootItem
                );

                // 2. Claim / verify idempotency key.
                transaction.addPutItem(
                        table,
                        TransactPutItemEnhancedRequest
                                .builder(
                                        NotificationTableItem.class
                                )
                                .item(idempotencyItem)
                                .conditionExpression(
                                        idempotencyCondition
                                )
                                .build()
                );

                // 3. Save all DeliveryAttempt children.
                for (NotificationTableItem attemptItem
                        : attemptItems) {

                    transaction.addPutItem(
                            table,
                            attemptItem
                    );
                }
            });
        } catch (TransactionCanceledException ex) {
            handleTransactionFailure(notification, ex);
        }
    }

    @Override
    public Notification[] fetchReadyToDispatch() {
        return new Notification[0];
    }

    public Optional<NotificationId> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
        Key key = Key.builder()
                .partitionValue(
                        NotificationTableKeys.idempotencyPk(
                                idempotencyKey
                        )
                )
                .sortValue(
                        NotificationTableKeys.idempotencySk()
                )
                .build();

        NotificationTableItem item =
                table.getItem(
                        request -> request.key(key)
                );

        if (item == null) {
            return Optional.empty();
        }

        return Optional.of(
                new NotificationId(
                        UUID.fromString(
                                item.getNotificationId()
                        )
                )
        );
    }

    private Expression idempotencyCondition(
            Notification notification) {

        return Expression.builder()
                .expression(
                        "attribute_not_exists(#pk) "
                                + "OR #notificationId = :notificationId"
                )
                .putExpressionName(
                        "#pk",
                        "PK"
                )
                .putExpressionName(
                        "#notificationId",
                        "NotificationId"
                )
                .putExpressionValue(
                        ":notificationId",
                        AttributeValue.fromS(
                                notification.id()
                                        .value()
                                        .toString()
                        )
                )
                .build();
    }

    private void handleTransactionFailure(
            Notification notification,
            TransactionCanceledException exception) {

        Optional<NotificationId> existingOwner =
                findByIdempotencyKey(
                        notification.idempotencyKey()
                );

        if (existingOwner.isPresent()
                && !existingOwner.get()
                .equals(notification.id())) {

            throw new DuplicateIdempotencyKeyException(
                    existingOwner.get()
            );
        }

        throw exception;
    }
}
