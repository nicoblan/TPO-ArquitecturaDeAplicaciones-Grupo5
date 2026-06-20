package com.uade.arquitectura.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotBlank String skuCode,
        @NotNull @Positive Integer quantity
) {
}
