package com.andreykan.notification.infrastructure.notification;

import com.andreykan.notification.application.notification.DuplicateIdempotencyKeyException;
import com.andreykan.notification.application.notification.NotificationRepository;
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
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.time.Instant;
import java.util.ArrayList;
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
