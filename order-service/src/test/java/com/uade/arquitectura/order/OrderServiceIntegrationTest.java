package com.uade.arquitectura.order;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.repository.OrderRepository;
import com.uade.arquitectura.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testPlaceOrder() {
        // Arrange
        Order order = Order.builder()
                .skuCode("IPHONE15")
                .quantity(1)
                .build();

        // Act
        Order savedOrder = orderService.placeOrder(order);

        // Assert
        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getOrderNumber());
        assertEquals("PENDING", savedOrder.getStatus());
        
        // Verificar que se envió el evento a RabbitMQ
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("order.created"), any(Order.class));
        
        // Verificar persistencia en H2
        assertTrue(orderRepository.findById(savedOrder.getId()).isPresent());
    }
}
