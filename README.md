# Order Service - Arquitectura de Aplicaciones

Este workspace contiene la Parte 1 del TP: el microservicio `order-service`.
El servicio permite crear pedidos, persistirlos, publicar `OrderCreatedEvent` en RabbitMQ y consumir `InventoryUpdatedEvent` para confirmar o rechazar la orden.

## Requisitos

- Java 17 compatible con Spring Boot 3.2.4
- Maven
- Docker o RabbitMQ local
- Un JWT firmado con el mismo secreto configurado en `JWT_SECRET`

## Variables de entorno

| Variable | Valor por defecto |
| --- | --- |
| `JWT_SECRET` | `clave-local-solo-para-desarrollo-order-service-hs256-32chars-minimo` |
| `EUREKA_URL` | `http://localhost:8761/eureka` |
| `RABBITMQ_HOST` | `localhost` |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USERNAME` | `guest` |
| `RABBITMQ_PASSWORD` | `guest` |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:orderdb` |
| `SPRING_DATASOURCE_USERNAME` | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | vacio |

No versionar secretos reales. El valor por defecto de `JWT_SECRET` es solo para desarrollo local.

## Levantar RabbitMQ

Desde la raiz del repo:

```bash
docker-compose up -d
```

La consola de RabbitMQ queda disponible en `http://localhost:15672` con `guest` / `guest`.

## Ejecutar tests

Desde `order-service`:

```bash
mvn clean test
```

Los tests usan H2, mockean el envio por RabbitMQ y deshabilitan Eureka/listeners para no requerir otros microservicios.

## Iniciar order-service

Desde `order-service`:

```bash
mvn spring-boot:run
```

El servicio escucha en `http://localhost:8082`.

## Verificar health

```bash
curl http://localhost:8082/actuator/health
```

`/actuator/health` es publico. Los endpoints `/api/orders/**` requieren JWT.

## Crear una orden

```bash
curl -i -X POST http://localhost:8082/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"skuCode":"IPHONE15","quantity":2}'
```

Respuesta esperada:

```json
{
  "id": 1,
  "orderNumber": "ORD-0c2b7b4a-6d61-4b4f-8f38-fca6f9cf0c01",
  "skuCode": "IPHONE15",
  "quantity": 2,
  "status": "PENDING",
  "createdAt": "2026-06-20T20:30:00Z"
}
```

## Consultar una orden

```bash
curl -i http://localhost:8082/api/orders/1 \
  -H "Authorization: Bearer <token>"
```

Si no existe, responde `404 Not Found`.

## Evento publicado

`order-service` publica en:

- Exchange: `order.exchange`
- Routing key: `order.created`

Payload `OrderCreatedEvent`:

```json
{
  "orderId": 1,
  "orderNumber": "ORD-0c2b7b4a-6d61-4b4f-8f38-fca6f9cf0c01",
  "skuCode": "IPHONE15",
  "quantity": 2,
  "createdAt": "2026-06-20T20:30:00Z"
}
```

## Evento consumido

`order-service` consume desde:

- Exchange: `inventory.exchange`
- Queue: `order.inventory-updated.queue`
- Routing key: `inventory.updated`

Payload `InventoryUpdatedEvent`:

```json
{
  "orderId": 1,
  "stockAvailable": true,
  "reason": null,
  "processedAt": "2026-06-20T20:30:03Z"
}
```

Si `stockAvailable` es `true`, la orden pasa a `CONFIRMED`.
Si es `false`, pasa a `REJECTED`.

## Integraciones pendientes

- `auth-service` debe emitir JWT compatibles con `JWT_SECRET` o con el mecanismo acordado.
- `inventory-service` debe consumir `OrderCreatedEvent` y publicar `InventoryUpdatedEvent`.
- API Gateway debe rutear `/api/orders/**` a `order-service` y propagar `Authorization`.
- Eureka Server debe estar disponible en `EUREKA_URL` para registro de servicio.
- `notification-service` puede integrarse luego consumiendo eventos sin cambiar este contrato.

## Documentacion adicional

- `docs/CONTRATO-INTEGRACION.md`
- `docs/APORTE-SAD-ORDER-SERVICE.md`
