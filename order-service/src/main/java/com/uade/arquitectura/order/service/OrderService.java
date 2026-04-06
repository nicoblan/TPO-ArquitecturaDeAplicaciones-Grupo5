package com.uade.arquitectura.order.service;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Order placeOrder(Order order) {
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Publicar evento para Notification Service e Inventory Service
        rabbitTemplate.convertAndSend("order.exchange", "order.created", savedOrder);
        
        return savedOrder;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }
}
