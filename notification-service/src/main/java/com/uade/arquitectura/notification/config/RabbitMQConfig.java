package com.uade.arquitectura.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATED_QUEUE = "order.created.notification";
    public static final String INVENTORY_UPDATED_QUEUE = "inventory.updated.notification";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.events", true, false);
    }

    @Bean
    public Queue orderCreatedNotificationQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderNotificationBinding(Queue orderCreatedNotificationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedNotificationQueue)
                .to(orderExchange)
                .with("order.created");
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange("inventory.events", true, false);
    }

    @Bean
    public Queue inventoryUpdatedNotificationQueue() {
        return new Queue(INVENTORY_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding inventoryNotificationBinding(Queue inventoryUpdatedNotificationQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryUpdatedNotificationQueue)
                .to(inventoryExchange)
                .with("inventory.updated");
    }
}
