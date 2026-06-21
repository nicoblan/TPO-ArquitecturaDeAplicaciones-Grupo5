package com.uade.arquitectura.inventory.messaging;

import java.time.Instant;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishInventoryUpdated(Long orderId, boolean stockAvailable, String reason) {
        InventoryUpdatedEvent event = new InventoryUpdatedEvent(orderId, stockAvailable, reason, Instant.now());
        rabbitTemplate.convertAndSend(
                "inventory.events",
                "inventory.updated",
                event
        );
    }

    public record InventoryUpdatedEvent(
            Long orderId,
            boolean stockAvailable,
            String reason,
            Instant processedAt
    ) {
    }
}
