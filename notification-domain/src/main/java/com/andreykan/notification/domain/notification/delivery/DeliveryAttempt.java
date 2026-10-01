package com.andreykan.notification.domain.notification.delivery;

import com.andreykan.notification.domain.notification.exceptions.InvalidStateTransitionException;

import java.time.Instant;
import java.util.Objects;

public class DeliveryAttempt {
    private final DeliveryAttemptNumber number;
    private DeliveryAttemptStatus status;
    private DeliveryFailure failure;
    private final Instant startedAt;
    private Instant succeededAt;
    private Instant failedAt;
    private String providerMessageId;

    public DeliveryAttempt(Instant now){
        this.number = new DeliveryAttemptNumber();
        this.status = DeliveryAttemptStatus.Started;
        this.startedAt = now;
    }

    public DeliveryAttempt(DeliveryAttemptNumber number, Instant now){
        this.number = number;
        this.status = DeliveryAttemptStatus.Started;
        this.startedAt = now;
    }

    public void markSucceeded(Instant succeededAt, String providerMessageId) {
        Objects.requireNonNull(succeededAt, "DeliveryAttempt succeededAt should not be null");

        if(status != DeliveryAttemptStatus.Started) throw new InvalidStateTransitionException("Cannot move delivery attempt status to Succeeded from Started");

        status = DeliveryAttemptStatus.Succeeded;
        this.succeededAt = succeededAt;
        this.providerMessageId = providerMessageId;
    }

    public void markFailed(DeliveryFailure failure, Instant failedAt) {
        Objects.requireNonNull(failure, "DeliveryAttempt failure should not be null");
        Objects.requireNonNull(failedAt, "DeliveryAttempt failedAt should not be null");

        if(status != DeliveryAttemptStatus.Started) throw new InvalidStateTransitionException("Cannot move delivery attempt status to Failed from Started");

        status = DeliveryAttemptStatus.Failed;
        this.failure = failure;
        this.failedAt = failedAt;
    }

    public DeliveryAttemptNumber number(){
        return number;
    }

    public DeliveryAttemptStatus status(){
        return status;
    }

    public DeliveryFailure failure(){
        return failure;
    }

    public Instant startedAt(){
        return startedAt;
    }

    public Instant succeededAt(){
        return succeededAt;
    }

    public Instant failedAt(){
        return failedAt;
    }

    public String providerMessageId() {
        return providerMessageId;
    }
}
