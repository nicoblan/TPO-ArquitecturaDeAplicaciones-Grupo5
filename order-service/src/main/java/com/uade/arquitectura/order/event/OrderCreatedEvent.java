package com.uade.arquitectura.order.event;

import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        String skuCode,
        Integer quantity,
        Instant createdAt
) {
}
