package com.uade.arquitectura.order.dto;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.domain.OrderStatus;

import java.time.Instant;

public record OrderResponse(
        Long id,
        String orderNumber,
        String skuCode,
        Integer quantity,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSkuCode(),
                order.getQuantity(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
