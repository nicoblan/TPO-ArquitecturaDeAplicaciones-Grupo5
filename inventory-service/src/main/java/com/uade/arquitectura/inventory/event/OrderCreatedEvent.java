package com.uade.arquitectura.inventory.event;

import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        String skuCode,
        Integer quantity,
        Instant createdAt
) {
}
