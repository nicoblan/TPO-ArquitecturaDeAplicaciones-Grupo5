package com.uade.arquitectura.order.messaging;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = "inventory.updated.queue")
    public void consumeInventoryUpdate(Order orderUpdate) {
        log.info("Recibido evento de inventario para orden: {}", orderUpdate.getOrderNumber());
        // En un caso real, aquí recibiríamos si hubo stock o no
        orderService.updateOrderStatus(orderUpdate.getId(), "CONFIRMED");
    }
}
