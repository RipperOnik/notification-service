package com.andreykan.notification.domain.entities;

import com.andreykan.notification.domain.valueobjects.deliveryattempt.DeliveryAttemptId;
import com.andreykan.notification.domain.valueobjects.deliveryattempt.DeliveryAttemptNumber;
import com.andreykan.notification.domain.valueobjects.deliveryattempt.DeliveryAttemptStatus;

import java.time.Instant;

public class DeliveryAttempt {
    private DeliveryAttemptId id;
    private DeliveryAttemptNumber number;
    private DeliveryAttemptStatus status;
    private Instant startedAt;
    private Instant succeededAt;
    private Instant failedAt;
    private String failureReason;
}
