package com.uade.arquitectura.notification.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = "order.created.notification")
    public void handleOrderCreated(Map<String, Object> message) {
        logger.info("[NOTIFICATION] Nuevo pedido creado: orderId={}, skuCode={}, quantity={}",
                message.get("orderId"), message.get("skuCode"), message.get("quantity"));
        // Simular envío de notificación (email, SMS, push)
    }

    @RabbitListener(queues = "inventory.updated.notification")
    public void handleInventoryUpdated(Map<String, Object> message) {
        logger.info("[NOTIFICATION] Inventario actualizado: orderId={}, stockAvailable={}",
                message.get("orderId"), message.get("stockAvailable"));
        // Simular envío de notificación
    }
}
