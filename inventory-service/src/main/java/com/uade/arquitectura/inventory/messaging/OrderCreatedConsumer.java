package com.uade.arquitectura.inventory.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    @Autowired
    private InventoryPublisher inventoryPublisher;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(String message) {
        logger.info("Recibido evento order.created: {}", message);
        
        // Simular procesamiento del inventario
        try {
            // En una implementación real, parseríamos el JSON y actualizaríamos el inventario
            String orderId = extractOrderId(message);
            
            // Simular reserva de inventario (siempre exitosa en la demo)
            inventoryPublisher.publishInventoryUpdated(orderId, "RESERVED", 1);
            
            logger.info("Inventario reservado para orden: {}", orderId);
        } catch (Exception e) {
            logger.error("Error procesando orden", e);
            // Publicar evento de fallo
            inventoryPublisher.publishInventoryUpdated("unknown", "FAILED", 0);
        }
    }

    private String extractOrderId(String message) {
        // Buscar orderId en el JSON
        int start = message.indexOf("\"orderId\"");
        if (start != -1) {
            int valueStart = message.indexOf(":", start) + 1;
            int valueEnd = message.indexOf("\"", valueStart + 1);
            if (valueEnd > valueStart) {
                return message.substring(valueStart, valueEnd).replaceAll("[^a-zA-Z0-9-]", "");
            }
        }
        return "unknown";
    }
}
