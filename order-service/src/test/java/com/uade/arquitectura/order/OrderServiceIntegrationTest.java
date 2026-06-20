package com.uade.arquitectura.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.arquitectura.order.config.RabbitMQConfig;
import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.domain.OrderStatus;
import com.uade.arquitectura.order.dto.CreateOrderRequest;
import com.uade.arquitectura.order.event.InventoryUpdatedEvent;
import com.uade.arquitectura.order.event.OrderCreatedEvent;
import com.uade.arquitectura.order.messaging.InventoryConsumer;
import com.uade.arquitectura.order.repository.OrderRepository;
import com.uade.arquitectura.order.service.OrderService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryConsumer inventoryConsumer;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CapturingRabbitTemplate rabbitTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        rabbitTemplate.sentMessages.clear();
        orderRepository.deleteAll();
    }

    @Test
    void createOrderPersistsPendingAndPublishesEvent() {
        Order savedOrder = orderService.createOrder(new CreateOrderRequest("IPHONE15", 1));

        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getOrderNumber());
        assertTrue(savedOrder.getOrderNumber().startsWith("ORD-"));
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertTrue(orderRepository.findById(savedOrder.getId()).isPresent());

        assertEquals(1, rabbitTemplate.sentMessages.size());
        CapturedMessage capturedMessage = rabbitTemplate.sentMessages.get(0);
        assertEquals(RabbitMQConfig.ORDER_EXCHANGE, capturedMessage.exchange);
        assertEquals(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, capturedMessage.routingKey);
        OrderCreatedEvent event = assertInstanceOf(OrderCreatedEvent.class, capturedMessage.payload);
        assertEquals(savedOrder.getId(), event.orderId());
        assertEquals(savedOrder.getOrderNumber(), event.orderNumber());
        assertEquals("IPHONE15", event.skuCode());
        assertEquals(1, event.quantity());
        assertNotNull(event.createdAt());
    }

    @Test
    void consumingInventoryEventWithStockConfirmsPendingOrder() {
        Order order = savePendingOrder("ORD-STOCK-OK");

        inventoryConsumer.consumeInventoryUpdate(new InventoryUpdatedEvent(order.getId(), true, null, Instant.now()));

        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void consumingInventoryEventWithoutStockRejectsPendingOrder() {
        Order order = savePendingOrder("ORD-STOCK-NO");

        inventoryConsumer.consumeInventoryUpdate(new InventoryUpdatedEvent(order.getId(), false, "No stock", Instant.now()));

        assertEquals(OrderStatus.REJECTED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void finalizedOrderIsNotReprocessed() {
        Order order = Order.builder()
                .orderNumber("ORD-FINALIZED")
                .skuCode("IPHONE15")
                .quantity(1)
                .status(OrderStatus.CONFIRMED)
                .build();
        order = orderRepository.save(order);

        inventoryConsumer.consumeInventoryUpdate(new InventoryUpdatedEvent(order.getId(), false, "Late event", Instant.now()));

        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void endpointRejectsZeroQuantity() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOrderRequest("IPHONE15", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endpointRejectsEmptySkuCode() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOrderRequest("", 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMissingOrderReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999999")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void ordersEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private Order savePendingOrder(String orderNumber) {
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .skuCode("IPHONE15")
                .quantity(1)
                .status(OrderStatus.PENDING)
                .build();
        return orderRepository.save(order);
    }

    private String token() {
        return Jwts.builder()
                .setSubject("test-user")
                .setExpiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    @TestConfiguration
    static class TestRabbitConfiguration {

        @Bean
        @Primary
        CapturingRabbitTemplate rabbitTemplate() {
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
