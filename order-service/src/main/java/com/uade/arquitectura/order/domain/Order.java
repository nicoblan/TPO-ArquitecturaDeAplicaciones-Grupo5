package com.uade.arquitectura.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String status; // PENDING, CONFIRMED, REJECTED
}
