# order-service

Microservicio de pedidos del TP de Arquitectura de Aplicaciones.

Para el contrato de integracion y el material del SAD, ver:

- `../docs/CONTRATO-INTEGRACION.md`
- `../docs/APORTE-SAD-ORDER-SERVICE.md`

## Comandos principales

```bash
mvn clean test
```

```bash
mvn spring-boot:run
```

Health publico:

```bash
curl http://localhost:8082/actuator/health
```

Crear orden:

```bash
curl -i -X POST http://localhost:8082/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"skuCode":"IPHONE15","quantity":2}'
```
