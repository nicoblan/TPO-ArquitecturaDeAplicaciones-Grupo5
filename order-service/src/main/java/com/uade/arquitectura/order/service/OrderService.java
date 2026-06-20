package com.uade.arquitectura.order.service;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Order placeOrder(Order order) {
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Publicar evento order.created para que inventory y notification lo consuman
        rabbitTemplate.convertAndSend("order.events", "order.created", savedOrder);
        
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
