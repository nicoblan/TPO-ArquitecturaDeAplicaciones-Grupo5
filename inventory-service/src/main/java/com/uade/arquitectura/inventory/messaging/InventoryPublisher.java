package com.uade.arquitectura.inventory.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishInventoryUpdated(String orderId, String status, int quantityReserved) {
        InventoryUpdatedEvent event = new InventoryUpdatedEvent(orderId, status, quantityReserved);
        rabbitTemplate.convertAndSend(
                "inventory.events",
                "inventory.updated",
                event
        );
    }

    public static class InventoryUpdatedEvent {
        public String orderId;
        public String status;
        public int quantityReserved;
        public long timestamp;

        public InventoryUpdatedEvent(String orderId, String status, int quantityReserved) {
            this.orderId = orderId;
            this.status = status;
            this.quantityReserved = quantityReserved;
            this.timestamp = System.currentTimeMillis();
        }

        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public int getQuantityReserved() { return quantityReserved; }
        public long getTimestamp() { return timestamp; }
    }
}
