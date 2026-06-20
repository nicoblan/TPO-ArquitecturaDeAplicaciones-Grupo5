package com.uade.arquitectura.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Order Events
    public static final String ORDER_EVENTS_EXCHANGE = "order.events";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // Inventory Events
    public static final String INVENTORY_EVENTS_EXCHANGE = "inventory.events";
    public static final String INVENTORY_UPDATED_QUEUE = "inventory.updated.queue";
    public static final String INVENTORY_UPDATED_ROUTING_KEY = "inventory.updated";

    // Order Exchange & Queues
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EVENTS_EXCHANGE, true, false);
    }

    // Inventory Exchange & Queue
    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue inventoryUpdatedQueue() {
        return new Queue(INVENTORY_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryUpdatedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryUpdatedQueue)
                .to(inventoryExchange)
                .with(INVENTORY_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
