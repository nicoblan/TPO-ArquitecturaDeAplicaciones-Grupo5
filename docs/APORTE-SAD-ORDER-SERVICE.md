# Aporte SAD - order-service

## Responsabilidad

`order-service` es responsable de recibir solicitudes de pedidos, persistirlas con estado inicial `PENDING`, publicar un evento de orden creada y actualizar el estado final cuando inventario informa el resultado de la reserva.

## Componentes internos

- `OrderController`: expone los endpoints REST obligatorios.
- `OrderService`: contiene la logica transaccional de creacion y actualizacion de ordenes.
- `OrderRepository`: acceso JPA a la tabla `orders`.
- `InventoryConsumer`: consume eventos de inventario.
- `RabbitMQConfig`: centraliza exchanges, routing keys, queue, bindings y conversion JSON.
- `JwtAuthenticationFilter` y `SecurityConfig`: validan JWT y mantienen seguridad stateless.
- `GlobalExceptionHandler`: normaliza respuestas de error.

## Dependencias

- Spring Boot Web para REST.
- Spring Data JPA para persistencia.
- H2 para la PoC local.
- Spring AMQP para RabbitMQ.
- Spring Security y JJWT para validar tokens.
- Spring Cloud Netflix Eureka Client para registrarse en Eureka.
- Spring Boot Actuator para health check.

## Persistencia

La entidad `Order` se guarda en la tabla `orders` e incluye:

- `id`
- `orderNumber`
- `skuCode`
- `quantity`
- `status`
- `createdAt`
- `updatedAt`

`status` usa el enum `OrderStatus` con valores `PENDING`, `CONFIRMED` y `REJECTED`. El numero de orden se genera automaticamente con prefijo `ORD-` y UUID.

## Comunicacion REST

El servicio expone solo:

- `POST /api/orders`
- `GET /api/orders/{id}`

No se expone la entidad JPA directamente. El contrato REST usa `CreateOrderRequest` y `OrderResponse`.

## Comunicacion asincronica

Al crear una orden se publica `OrderCreatedEvent` en RabbitMQ:

- Exchange: `order.exchange`
- Routing key: `order.created`

Para recibir la respuesta de inventario, el servicio consume `InventoryUpdatedEvent`:

- Exchange: `inventory.exchange`
- Queue: `order.inventory-updated.queue`
- Routing key: `inventory.updated`

Los eventos son DTOs independientes y no acoplan RabbitMQ a entidades JPA.

## Consistencia eventual

El alta de la orden y la reserva de inventario no ocurren en una unica transaccion distribuida. La orden queda `PENDING` hasta recibir el resultado asincronico del inventario.

Esta implementacion es una PoC de comunicacion asincronica y consistencia eventual. No implementa una Saga completa con compensaciones, orquestador, reintentos persistidos ni outbox transaccional.

## Seguridad

El servicio es stateless. `/actuator/health` es publico y `/api/orders/**` requiere JWT. El secreto se configura mediante `JWT_SECRET` para que pueda coincidir con el futuro `auth-service` sin versionar claves reales.

## Decisiones arquitectonicas

- Se mantuvo H2 para simplificar la PoC.
- Se centralizaron nombres de RabbitMQ en constantes de configuracion.
- Se uso JSON como formato de mensaje para facilitar integracion entre equipos.
- Se agrego idempotencia minima: una orden finalizada no se reprocesa.
- Se evito implementar Gateway, Eureka Server, Auth, Inventory o Notification dentro de este servicio.

## Riesgos

- Si RabbitMQ no esta disponible al crear la orden, la publicacion puede fallar y afectar el flujo.
- No hay patron outbox, por lo que no existe garantia fuerte entre commit de base y publicacion de evento.
- La validacion JWT depende de que todos los servicios usen el mismo secreto o mecanismo de firma.
- H2 en memoria no conserva datos entre reinicios.

## Limitaciones de la PoC

- No hay reintentos configurados para eventos fallidos.
- No hay DLQ.
- No hay trazabilidad distribuida.
- No se implementa reserva real de stock.
- No hay API Gateway ni auth-service en este modulo.
