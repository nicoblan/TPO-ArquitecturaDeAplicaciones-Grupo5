package com.uade.arquitectura.inventory.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.uade.arquitectura.inventory.event.OrderCreatedEvent;

@Component
public class OrderCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    @Autowired
    private InventoryPublisher inventoryPublisher;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("Recibido evento order.created: orderId={}, skuCode={}, quantity={}",
                event.orderId(), event.skuCode(), event.quantity());

        try {
            // Simulación de reserva de inventario (siempre exitosa en la demo)
            inventoryPublisher.publishInventoryUpdated(event.orderId(), true, "Stock reservado");
            logger.info("Inventario reservado para orden: {}", event.orderId());
        } catch (Exception e) {
            logger.error("Error procesando orden {}", event.orderId(), e);
            inventoryPublisher.publishInventoryUpdated(event.orderId(), false, "Error al reservar stock");
        }
    }
}
