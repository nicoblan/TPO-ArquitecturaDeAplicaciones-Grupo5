# 🛒 Order Service - Proyecto Arquitectura de Aplicaciones (UADE 2026)

Este microservicio es responsable de la **gestión de pedidos** dentro del ecosistema de microservicios de la materia.

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

---

## ⚙️ Cómo ejecutar

1. Asegúrate de tener **Docker Desktop** corriendo.

2. Levanta la infraestructura necesaria (RabbitMQ):

```bash
docker-compose up -d
