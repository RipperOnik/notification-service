package com.andreykan.notification.domain.notification.retry;

import java.time.Duration;

public record RetryPolicy(int maxAttempts, Duration initialDelay, Duration maxDelay) {
    public boolean canRetry(int attemptsMade) {
        return attemptsMade < maxAttempts;
    }

    public Duration retryDelay(int attemptsMade) {
        long multiplier = 1L << (attemptsMade - 1);

        Duration delay =
                initialDelay.multipliedBy(multiplier);

        return delay.compareTo(maxDelay) > 0
                ? maxDelay
                : delay;
    }
}
