package com.andreykan.notification.infrastructure.notification;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class NotificationTableItem {

    public static final String READY_INDEX =
            "status-readyAt-index";

    private String pk;
    private String sk;
    private String itemType;

    // Root Notification fields
    private String notificationId;
    private String idempotencyKey;
    private String status;
    private String recipientAddress;
    private String subject;
    private String payload;
    private String channel;

    private Long createdAt;
    private Long readyAt;
    private Long failedAt;
    private Long deliveredAt;

    // DeliveryAttempt fields
    private Integer attemptNumber;
    private String failureType;
    private String failureReason;
    private String providerMessageId;

    private Long startedAt;
    private Long succeededAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDbAttribute("ItemType")
    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @DynamoDbAttribute("NotificationId")
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    @DynamoDbAttribute("IdempotencyKey")
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = READY_INDEX)
    @DynamoDbAttribute("Status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDbSecondarySortKey(indexNames = READY_INDEX)
    @DynamoDbAttribute("ReadyAt")
    public Long getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Long readyAt) {
        this.readyAt = readyAt;
    }

    @DynamoDbAttribute("RecipientAddress")
    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    @DynamoDbAttribute("Subject")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @DynamoDbAttribute("Payload")
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @DynamoDbAttribute("Channel")
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @DynamoDbAttribute("CreatedAt")
    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("FailedAt")
    public Long getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Long failedAt) {
        this.failedAt = failedAt;
    }

    @DynamoDbAttribute("DeliveredAt")
    public Long getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Long deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    @DynamoDbAttribute("AttemptNumber")
    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    @DynamoDbAttribute("FailureType")
    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    @DynamoDbAttribute("FailureReason")
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @DynamoDbAttribute("ProviderMessageId")
    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    @DynamoDbAttribute("StartedAt")
    public Long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Long startedAt) {
        this.startedAt = startedAt;
    }

    @DynamoDbAttribute("SucceededAt")
    public Long getSucceededAt() {
        return succeededAt;
    }

    public void setSucceededAt(Long succeededAt) {
        this.succeededAt = succeededAt;
    }
}