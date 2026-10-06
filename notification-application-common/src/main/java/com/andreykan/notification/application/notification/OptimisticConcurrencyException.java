package com.andreykan.notification.application.notification;

import com.andreykan.notification.domain.notification.NotificationId;

public class OptimisticConcurrencyException
        extends RuntimeException {

    public OptimisticConcurrencyException(
            NotificationId id,
            long expectedVersion,
            long actualVersion
    ) {
        super(
                ("Notification %s was modified concurrently. "
                        + "Expected version %d but current version is %d.")
                        .formatted(
                                id,
                                expectedVersion,
                                actualVersion
                        )
        );
    }
}