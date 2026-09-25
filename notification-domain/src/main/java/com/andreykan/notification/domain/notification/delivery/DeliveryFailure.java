package com.andreykan.notification.domain.notification.delivery;

public record DeliveryFailure(DeliveryFailureType type, String reason) {
}
