package com.uade.arquitectura.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))
                // Order Service
                .route("order-service", r -> r
                        .path("/orders/**")
                        .uri("lb://order-service"))
                // Inventory Service
                .route("inventory-service", r -> r
                        .path("/inventory/**")
                        .uri("lb://inventory-service"))
                // Notification Service
                .route("notification-service", r -> r
                        .path("/notifications/**")
                        .uri("lb://notification-service"))
                .build();
    }

}
