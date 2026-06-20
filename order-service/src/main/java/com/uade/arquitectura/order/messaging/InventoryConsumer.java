package com.uade.arquitectura.order.messaging;

import com.uade.arquitectura.order.config.RabbitMQConfig;
import com.uade.arquitectura.order.event.InventoryUpdatedEvent;
import com.uade.arquitectura.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    private final OrderService orderService;

    public InventoryConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_UPDATED_QUEUE)
    public void consumeInventoryUpdate(InventoryUpdatedEvent event) {
        log.info("Inventory update received for order id {}", event.orderId());
        orderService.handleInventoryUpdated(event);
    }
}
