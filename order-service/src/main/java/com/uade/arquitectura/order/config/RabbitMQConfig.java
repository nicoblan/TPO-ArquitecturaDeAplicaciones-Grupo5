package com.uade.arquitectura.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String INVENTORY_UPDATED_QUEUE = "inventory.updated.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";

    @Bean
    public Queue orderCreatedQueue() { return new Queue(ORDER_CREATED_QUEUE); }

    @Bean
    public Queue inventoryUpdatedQueue() { return new Queue(INVENTORY_UPDATED_QUEUE); }

    @Bean
    public TopicExchange orderExchange() { return new TopicExchange(ORDER_EXCHANGE); }

    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with("order.created");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
