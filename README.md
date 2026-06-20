# 🛒 Order Service - Proyecto Arquitectura de Aplicaciones (UADE 2026)

Este workspace contiene el microservicio nuevo del TP: **order-service**. El ecosistema base de la consigna debe existir en módulos o repositorios aparte para completar la demo total.

---

## 🚀 Tecnologías

- **Java 17** con **Spring Boot 3.2.4**
- **Spring Cloud** (Eureka Client, Gateway support)
- **Spring Security + JWT** (Validación de tokens descentralizada)
- **RabbitMQ** (Comunicación asíncrona)
- **JPA / Hibernate** con base de datos **H2** (en memoria)

---

## 🏗️ Arquitectura

El servicio sigue el patrón **Database per Service** y utiliza una **arquitectura en capas** tradicional.

Se integra de forma asíncrona con el resto del ecosistema mediante el patrón:

- **Saga (Coreografía)** → para asegurar la **consistencia eventual** con el inventario.

## ✅ Alcance cubierto en este workspace

- Registro en Eureka mediante `spring-cloud-starter-netflix-eureka-client`.
- Endpoint REST protegido con JWT.
- Persistencia de pedidos en H2.
- Publicación de evento `order.created` en RabbitMQ.
- Consumo de evento desde `inventory.updated.queue`.

## ⚠️ Falta para completar toda la consigna

- `config-server`
- `eureka`
- `gateway`
- `auth`
- `inventory`
- `notification`
- Flujo end-to-end con broker y gateway levantados juntos

---

## ⚙️ Cómo ejecutar

1. Asegúrate de tener **Docker Desktop** corriendo.

2. Levanta la infraestructura necesaria (RabbitMQ):

```bash
docker-compose up -d
