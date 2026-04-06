# Order Service - Proyecto Arquitectura de Aplicaciones (UADE 2026)

Este microservicio es responsable de la gestión de pedidos dentro del ecosistema de microservicios de la materia.

## 🚀 Tecnologías
- **Java 17** con **Spring Boot 3.2.4**
- **Spring Cloud** (Eureka Client, Gateway support)
- **Spring Security + JWT** (Validación de tokens descentralizada)
- **RabbitMQ** (Comunicación asíncrona)
- **JPA / Hibernate** con base de datos H2 (en memoria)

## 🏗️ Arquitectura
El servicio sigue el patrón **Database per Service** y utiliza una arquitectura de capas tradicional, integrándose asíncronamente con el resto del ecosistema mediante el patrón **Saga (Coreografía)** para asegurar la consistencia eventual con el inventario.

## ⚙️ Cómo ejecutar
1. Asegúrate de tener **Docker Desktop** corriendo.
2. Levanta la infraestructura necesaria (RabbitMQ):
   ```bash
   docker-compose up -d
   ```
3. Compila y ejecuta el servicio:
   ```bash
   mvn clean install
   ```
   ```bash
   mvn spring-boot:run
   ```

## 🔒 Seguridad
Los endpoints bajo `/api/orders/**` requieren un token JWT válido emitido por el `auth-service`. 
El token debe enviarse en el header:
`Authorization: Bearer <TU_TOKEN>`

## 📈 Endpoints
- `POST /api/orders`: Crea un nuevo pedido y publica el evento `order.created` en RabbitMQ.
- `GET /api/orders/test`: Endpoint de verificación de salud y seguridad.
