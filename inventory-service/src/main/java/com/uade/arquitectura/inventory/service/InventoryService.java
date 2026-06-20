package com.uade.arquitectura.inventory.service;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    public boolean reserveInventory(String productId, int quantity) {
        // Simular lógica de reserva
        return true;
    }

    public boolean releaseInventory(String productId, int quantity) {
        // Simular lógica de liberación
        return true;
    }
}
