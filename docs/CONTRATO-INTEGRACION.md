# Contrato de integracion - order-service

## Servicio

- Nombre del servicio: `order-service`
- Puerto local: `8082`
- Nombre registrado en Eureka: `order-service`
- Base path REST: `/api/orders`
- Health check publico: `/actuator/health`

## Autenticacion

Los endpoints `/api/orders/**` requieren JWT en el header:

```http
Authorization: Bearer <token>
```

El token debe estar firmado con el secreto compartido por el futuro `auth-service`.
El secreto se lee desde `JWT_SECRET`. No se debe versionar un secreto real.

## Endpoints

### Crear orden

```http
POST /api/orders
Content-Type: application/json
Authorization: Bearer <token>
```

Request:

```json
{
  "skuCode": "IPHONE15",
  "quantity": 2
}
```

Respuesta `201 Created`:

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

Validaciones:

- `skuCode` es obligatorio y no puede estar vacio.
- `quantity` es obligatorio y debe ser mayor que cero.

### Consultar orden

```http
GET /api/orders/{id}
Authorization: Bearer <token>
```

Respuesta `200 OK`:

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

Si no existe, responde `404 Not Found`.

## Evento publicado por order-service

- Exchange: `order.events`
- Routing key: `order.created`
- Tipo logico: `OrderCreatedEvent`

JSON:

```json
{
  "orderId": 1,
  "orderNumber": "ORD-0c2b7b4a-6d61-4b4f-8f38-fca6f9cf0c01",
  "skuCode": "IPHONE15",
  "quantity": 2,
  "createdAt": "2026-06-20T20:30:00Z"
}
```

## Evento consumido por order-service

- Exchange: `inventory.events`
- Queue: `order.inventory-updated.queue`
- Routing key: `inventory.updated`
- Tipo logico: `InventoryUpdatedEvent`

JSON:

```json
{
  "orderId": 1,
  "stockAvailable": true,
  "reason": null,
  "processedAt": "2026-06-20T20:30:03Z"
}
```

Reglas:

- `stockAvailable = true`: la orden pasa de `PENDING` a `CONFIRMED`.
- `stockAvailable = false`: la orden pasa de `PENDING` a `REJECTED`.
- Si la orden no existe, se registra un error y no se crea ninguna orden nueva.
- Si la orden ya esta en `CONFIRMED` o `REJECTED`, el evento se ignora para mantener idempotencia minima.

## Variables de entorno

| Variable | Valor por defecto | Uso |
| --- | --- | --- |
| `JWT_SECRET` | `clave-local-solo-para-desarrollo-order-service-hs256-32chars-minimo` | Firma/verificacion HS256 de JWT |
| `EUREKA_URL` | `http://localhost:8761/eureka` | URL del Eureka Server |
| `RABBITMQ_HOST` | `localhost` | Host del broker RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Puerto AMQP |
| `RABBITMQ_USERNAME` | `guest` | Usuario RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Password RabbitMQ |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:orderdb` | URL de base de datos |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Usuario de base de datos |
| `SPRING_DATASOURCE_PASSWORD` | vacio | Password de base de datos |

## Flujo esperado

1. API Gateway envia `POST /api/orders` con JWT valido.
2. `order-service` valida el request y persiste una orden `PENDING`.
3. `order-service` publica `OrderCreatedEvent` en `order.exchange` con routing key `order.created`.
4. `inventory-service` consume ese evento, intenta reservar stock y publica `InventoryUpdatedEvent`.
5. `order-service` consume `InventoryUpdatedEvent` desde `order.inventory-updated.queue`.
6. La orden queda `CONFIRMED` si hubo stock o `REJECTED` si no hubo stock.

## Trabajo pendiente de inventory-service

El siguiente integrante debe:

- Crear una cola propia enlazada a `order.exchange` con routing key `order.created`.
- Consumir `OrderCreatedEvent`.
- Reservar o validar stock para `skuCode` y `quantity`.
- Publicar `InventoryUpdatedEvent` en `inventory.exchange` con routing key `inventory.updated`.
- Respetar el campo `orderId` recibido para que `order-service` actualice la orden correcta.

## Trabajo pendiente del API Gateway

El siguiente integrante debe:

- Rutear `/api/orders/**` hacia `order-service`.
- Propagar el header `Authorization`.
- Dejar `/actuator/health` disponible segun el criterio de monitoreo del ecosistema.
- Coordinar el mismo `JWT_SECRET` o mecanismo de validacion con `auth-service` y `order-service`.
