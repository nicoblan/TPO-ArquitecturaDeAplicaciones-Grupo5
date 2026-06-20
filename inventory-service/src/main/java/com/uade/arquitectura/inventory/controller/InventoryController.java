package com.uade.arquitectura.inventory.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getInventory(@PathVariable String productId) {
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("quantity", 100); // Cantidad fija para demo
        response.put("reserved", 0);
        return ResponseEntity.ok(response);
    }
}
