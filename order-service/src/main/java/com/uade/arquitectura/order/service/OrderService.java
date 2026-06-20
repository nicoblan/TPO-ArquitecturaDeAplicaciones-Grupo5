package com.uade.arquitectura.order.service;

import com.uade.arquitectura.order.config.RabbitMQConfig;
import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.domain.OrderStatus;
import com.uade.arquitectura.order.dto.CreateOrderRequest;
import com.uade.arquitectura.order.event.InventoryUpdatedEvent;
import com.uade.arquitectura.order.event.OrderCreatedEvent;
import com.uade.arquitectura.order.exception.OrderNotFoundException;
import com.uade.arquitectura.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .skuCode(request.skuCode())
                .quantity(request.quantity())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        publishOrderCreated(savedOrder);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    public void handleInventoryUpdated(InventoryUpdatedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);

        if (order == null) {
            log.error("Inventory update received for missing order id {}", event.orderId());
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("Ignoring inventory update for finalized order id {} with status {}", order.getId(), order.getStatus());
            return;
        }

        order.setStatus(event.stockAvailable() ? OrderStatus.CONFIRMED : OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    private void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getSkuCode(),
                order.getQuantity(),
                order.getCreatedAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                event
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID();
    }
}
