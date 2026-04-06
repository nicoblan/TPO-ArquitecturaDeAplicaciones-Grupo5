package com.uade.arquitectura.order.controller;

import com.uade.arquitectura.order.domain.Order;
import com.uade.arquitectura.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.placeOrder(order));
    }

    @GetMapping("/test")
    public String test() {
        return "Order service is up and secured!";
    }
}
