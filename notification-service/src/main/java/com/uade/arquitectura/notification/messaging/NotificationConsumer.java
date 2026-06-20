package com.uade.arquitectura.notification.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = "order.created.notification")
    public void handleOrderCreated(String message) {
        logger.info("[NOTIFICATION] Nuevo pedido creado: {}", message);
        // Simular envío de notificación (email, SMS, push)
    }

    @RabbitListener(queues = "inventory.updated.notification")
    public void handleInventoryUpdated(String message) {
        logger.info("[NOTIFICATION] Inventario actualizado: {}", message);
        // Simular envío de notificación
    }
}
