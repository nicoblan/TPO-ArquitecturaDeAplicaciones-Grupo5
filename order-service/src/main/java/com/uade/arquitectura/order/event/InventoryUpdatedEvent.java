package com.uade.arquitectura.order.event;

import java.time.Instant;

public record InventoryUpdatedEvent(
        Long orderId,
        boolean stockAvailable,
        String reason,
        Instant processedAt
) {
}
