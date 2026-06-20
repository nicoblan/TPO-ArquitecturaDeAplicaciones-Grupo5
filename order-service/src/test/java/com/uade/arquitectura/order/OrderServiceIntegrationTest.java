package com.uade.arquitectura.order;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.repository.OrderRepository;
import com.uade.arquitectura.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CapturingRabbitTemplate rabbitTemplate;

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
        assertEquals(1, rabbitTemplate.sentMessages.size());
        CapturedMessage capturedMessage = rabbitTemplate.sentMessages.get(0);
        assertEquals("order.exchange", capturedMessage.exchange);
        assertEquals("order.created", capturedMessage.routingKey);
        assertTrue(capturedMessage.payload instanceof Order);
        
        // Verificar persistencia en H2
        assertTrue(orderRepository.findById(savedOrder.getId()).isPresent());
    }

    @TestConfiguration
    static class TestRabbitConfiguration {

        @Bean
        @Primary
        public CapturingRabbitTemplate rabbitTemplate() {
            return new CapturingRabbitTemplate(new CachingConnectionFactory());
        }
    }

    static class CapturingRabbitTemplate extends RabbitTemplate {
        private final List<CapturedMessage> sentMessages = new ArrayList<>();

        CapturingRabbitTemplate(ConnectionFactory connectionFactory) {
            super(connectionFactory);
        }

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            sentMessages.add(new CapturedMessage(exchange, routingKey, message));
        }
    }

    record CapturedMessage(String exchange, String routingKey, Object payload) {
    }
}
