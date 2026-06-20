package com.uade.arquitectura.order.messaging;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private final OrderService orderService;

    public InventoryConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "inventory.updated.queue")
    public void consumeInventoryUpdate(Order orderUpdate) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InventoryConsumer.class);
        log.info("Recibido evento de inventario para orden: {}", orderUpdate.getOrderNumber());
        // En un caso real, aquí recibiríamos si hubo stock o no
        orderService.updateOrderStatus(orderUpdate.getId(), "CONFIRMED");
    }
}
