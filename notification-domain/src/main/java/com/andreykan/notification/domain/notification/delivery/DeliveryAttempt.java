package com.andreykan.notification.domain.notification.delivery;

import com.andreykan.notification.domain.notification.exceptions.InvalidStateTransitionException;

import java.time.Instant;
import java.util.Objects;

public class DeliveryAttempt {
    private DeliveryAttemptId id;
    private DeliveryAttemptNumber number;
    private DeliveryAttemptStatus status;
    private DeliveryFailure failure;
    private Instant startedAt;
    private Instant succeededAt;
    private Instant failedAt;

    public DeliveryAttempt(){
        this.id = DeliveryAttemptId.generate();
        this.number = new DeliveryAttemptNumber();
        this.status = DeliveryAttemptStatus.Started;
        this.startedAt = Instant.now();
    }

    public DeliveryAttempt(int number){
        this.id = DeliveryAttemptId.generate();
        this.number = new DeliveryAttemptNumber(number);
        this.status = DeliveryAttemptStatus.Started;
        this.startedAt = Instant.now();
    }

    public void markSucceeded(Instant succeededAt) {
        Objects.requireNonNull(succeededAt, "DeliveryAttempt succeededAt should not be null");

        if(status != DeliveryAttemptStatus.Started) throw new InvalidStateTransitionException("Cannot move delivery attempt status to Succeeded from Started");

        status = DeliveryAttemptStatus.Succeeded;
        this.succeededAt = succeededAt;
    }

    public void markFailed(DeliveryFailure failure, Instant failedAt) {
        Objects.requireNonNull(failure, "DeliveryAttempt failure should not be null");
        Objects.requireNonNull(failedAt, "DeliveryAttempt failedAt should not be null");

        if(status != DeliveryAttemptStatus.Started) throw new InvalidStateTransitionException("Cannot move delivery attempt status to Failed from Started");

        status = DeliveryAttemptStatus.Failed;
        this.failure = failure;
        this.failedAt = failedAt;
    }

    public DeliveryAttemptId id() {
        return id;
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
}
