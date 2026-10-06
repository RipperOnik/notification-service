package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.application.notification.DuplicateIdempotencyKeyException;
import com.andreykan.notification.application.notification.NotificationRepository;
import com.andreykan.notification.application.notification.OptimisticConcurrencyException;
import com.andreykan.notification.domain.notification.IdempotencyKey;
import com.andreykan.notification.domain.notification.Notification;
import com.andreykan.notification.domain.notification.NotificationId;
import com.andreykan.notification.domain.notification.NotificationStatus;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.time.Instant;
import java.util.*;

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
        long expectedVersion = notification.version();
        long nextVersion = expectedVersion + 1;

        boolean isNew = expectedVersion == 0;

        var rootItem =
                mapper.toNotificationItem(notification, nextVersion);

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

        Expression concurrencyCondition =
                optimisticConcurrencyCondition(expectedVersion);

        try {
            enhancedClient.transactWriteItems(transaction -> {

                // 1. Save aggregate root.
                transaction.addPutItem(
                        table,
                        TransactPutItemEnhancedRequest
                                .builder(NotificationTableItem.class)
                                .item(rootItem)
                                .conditionExpression(concurrencyCondition)
                                .returnValuesOnConditionCheckFailure(
                                        ReturnValuesOnConditionCheckFailure.ALL_OLD
                                )
                                .build()
                );

                // 2. Only claim the idempotency key
                //    when creating the Notification.
                if(isNew)
                {
                    var idempotencyItem =
                            mapper.toIdempotencyItem(notification);

                    transaction.addPutItem(
                            table,
                            TransactPutItemEnhancedRequest
                                    .builder(
                                            NotificationTableItem.class
                                    )
                                    .item(idempotencyItem)
                                    .conditionExpression(
                                            idempotencyCondition()
                                    )
                                    .returnValuesOnConditionCheckFailure(
                                            ReturnValuesOnConditionCheckFailure.ALL_OLD
                                    )
                                    .build()
                    );
                }

                // 3. Save all DeliveryAttempt children.
                for (NotificationTableItem attemptItem
                        : attemptItems) {

                    transaction.addPutItem(
                            table,
                            attemptItem
                    );
                }
            });

            // Only advance the in-memory version AFTER
            // DynamoDB successfully committed.
            notification.markPersisted(nextVersion);

        } catch (TransactionCanceledException ex) {
            handleTransactionFailure(notification, ex);
        }
    }

    private Expression optimisticConcurrencyCondition(
            long expectedVersion
    ) {
        if (expectedVersion == 0) {

            return Expression.builder()
                    .expression(
                            "attribute_not_exists(#pk)"
                    )
                    .expressionNames(
                            Map.of(
                                    "#pk",
                                    "PK"
                            )
                    )
                    .build();
        }

        return Expression.builder()
                .expression(
                        "#version = :expectedVersion"
                )
                .expressionNames(
                        Map.of(
                                "#version",
                                "Version"
                        )
                )
                .expressionValues(
                        Map.of(
                                ":expectedVersion",
                                AttributeValue.builder()
                                        .n(
                                                Long.toString(
                                                        expectedVersion
                                                )
                                        )
                                        .build()
                        )
                )
                .build();
    }

    @Override
    public List<Notification> fetchReadyToDispatch(Instant now) {
        var index = table.index("status-readyAt-index");

        List<NotificationTableItem> candidates =
                new ArrayList<>();

        candidates.addAll(
                queryReadyItems(
                        index,
                        NotificationStatus.Created,
                        now
                )
        );

        candidates.addAll(
                queryReadyItems(
                        index,
                        NotificationStatus.RetryPending,
                        now
                )
        );

        return candidates.stream()
                .map(NotificationTableItem::getNotificationId)
                .map(UUID::fromString)
                .map(NotificationId::new)
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public Optional<Notification> findById(
            NotificationId notificationId
    ) {

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(
                                        NotificationTableKeys
                                                .notificationPk(notificationId)
                                )
                                .build()
                );

        List<NotificationTableItem> items =
                new ArrayList<>();

        for (Page<NotificationTableItem> page :
                table.query(request -> request
                        .queryConditional(condition)
                )) {

            items.addAll(page.items());
        }

        if (items.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                mapper.toDomain(items)
        );
    }

    private List<NotificationTableItem> queryReadyItems(
            DynamoDbIndex<NotificationTableItem> index,
            NotificationStatus status,
            Instant now
    ) {

        QueryConditional condition =
                QueryConditional.sortLessThanOrEqualTo(
                        Key.builder()
                                .partitionValue(status.name())
                                .sortValue(now.toEpochMilli())
                                .build()
                );

        List<NotificationTableItem> result =
                new ArrayList<>();

        for (Page<NotificationTableItem> page :
                index.query(request -> request
                        .queryConditional(condition)
                )) {

            result.addAll(page.items());
        }

        return result;
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

    private Expression idempotencyCondition() {

        return Expression.builder()
                .expression(
                        "attribute_not_exists(#pk)"
                )
                .expressionNames(
                        Map.of(
                                "#pk",
                                "PK"
                        )
                )
                .build();
    }

    private void handleTransactionFailure(
            Notification notification,
            TransactionCanceledException exception) {

        for(CancellationReason reason: exception.cancellationReasons()) {
            if (!"ConditionalCheckFailed".equals(reason.code())) {
                continue;
            }

            if(!reason.hasItem()) {
                continue;
            }

            Map<String, AttributeValue> item = reason.item();

            String itemType = getString(item, "ItemType");

            if (NotificationItemType.NOTIFICATION.name().equals(itemType)) {
                long actualVersion =
                        getLong(item, "Version");

                throw new OptimisticConcurrencyException(
                        notification.id(),
                        notification.version(),
                        actualVersion
                );
            }

            if (NotificationItemType.IDEMPOTENCY.name().equals(itemType)) {

                String existingNotificationId =
                        getString(
                                item,
                                "NotificationId"
                        );

                throw new DuplicateIdempotencyKeyException(
                        new NotificationId(
                                UUID.fromString(
                                        existingNotificationId
                                )
                        )
                );
            }
        }

        // We couldn't safely translate it.
        throw exception;
    }

    private String getString(
            Map<String, AttributeValue> item,
            String attribute
    ) {
        AttributeValue value = item.get(attribute);

        if (value == null || value.s() == null) {
            throw new IllegalStateException(
                    "Missing DynamoDB attribute: " + attribute
            );
        }

        return value.s();
    }

    private long getLong(
            Map<String, AttributeValue> item,
            String attribute
    ) {
        AttributeValue value = item.get(attribute);

        if (value == null || value.n() == null) {
            throw new IllegalStateException(
                    "Missing DynamoDB attribute: " + attribute
            );
        }

        return Long.parseLong(value.n());
    }
}
