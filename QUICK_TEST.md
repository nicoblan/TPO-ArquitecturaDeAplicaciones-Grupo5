# 🧪 QUICK TEST GUIDE - Integración del Ecosistema

**Propósito:** Validar que la integración funciona correctamente

---

## ⚡ Inicio Rápido

```bash
# 1. Ejecutar ecosistema
docker-compose up --build

# 2. Esperar 30-45 segundos para que todos se registren
# 3. Ejecutar los tests siguientes
```

---

## 🔍 TEST 1: Verificar Eureka

**URL:** http://localhost:8761/eureka/apps

**Esperado:** Ver 6 servicios registrados
```
- AUTH-SERVICE
- ORDER-SERVICE
- INVENTORY-SERVICE
- NOTIFICATION-SERVICE
- API-GATEWAY
- CONFIG-SERVER
```

---

## 🔍 TEST 2: Verificar RabbitMQ

**URL:** http://localhost:15672

**Credenciales:** guest / guest

**Verificar:**
- Bajo "Exchanges": `order.events` y `inventory.events`
- Bajo "Queues": 
  - `order.created.queue`
  - `inventory.updated.queue`
  - `order.created.notification`
  - `inventory.updated.notification`

---

## 🔍 TEST 3: Obtener Token JWT

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "pass1"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user1",
  "expiresIn": "86400"
}
```

**Guardar el token para tests siguientes:**
```bash
TOKEN="<copiar el token de respuesta>"
```

---

## 🔍 TEST 4: Validar Token

```bash
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
{
  "valid": true,
  "username": "user1"
}
```

---

## 🔍 TEST 5: Crear una Orden

```bash
curl -X POST http://localhost:8080/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "customerId": "cust123",
    "productId": "prod456",
    "quantity": 5
  }'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "cust123",
  "productId": "prod456",
  "quantity": 5,
  "status": "PENDING"
}
```

**Guardar el ID de la orden:**
```bash
ORDER_ID="<copiar el id de respuesta>"
```

---

## 🔍 TEST 6: Consultar Orden

```bash
curl -X GET "http://localhost:8080/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado:** Orden devuelta (puede estar en PENDING o CONFIRMED)

---

## 🔍 TEST 7: Verificar Eventos en RabbitMQ

**En el dashboard de RabbitMQ:**

1. Ir a http://localhost:15672
2. Hacer clic en "Queues"
3. Observar:
   - `order.created.queue` debe tener mensajes (o procesados)
   - `inventory.updated.queue` puede tener mensajes

---

## 🔍 TEST 8: Verificar Inventario

```bash
curl -X GET http://localhost:8080/inventory/prod456
```

**Respuesta esperada:**
```json
{
  "productId": "prod456",
  "quantity": 100,
  "reserved": 0
}
```

---

## 🔍 TEST 9: Verificar Logs de Servicios

### Logs de Order Service
```bash
docker logs -f order-service | grep -i "order\|event"
```

**Esperado:** Ver "Creando orden" y "Publicando evento"

### Logs de Inventory Service
```bash
docker logs -f inventory-service | grep -i "order\|inventario"
```

**Esperado:** Ver "Recibido evento order.created" y "Inventario reservado"

### Logs de Notification Service
```bash
docker logs -f notification-service | grep -i "NOTIFICATION"
```

**Esperado:** Ver "[NOTIFICATION] Nuevo pedido creado"

---

## 🔍 TEST 10: Verificar Status de Servicios

```bash
# Auth Service
curl http://localhost:8080/auth/health

# Order Service (directo)
curl http://localhost:8082/orders/health

# Inventory Service (directo)
curl http://localhost:8083/inventory/health

# Gateway
curl http://localhost:8080/actuator/health
```

**Esperado:** `{"status":"UP"}`

---

## ✅ FLUJO COMPLETO DE PRUEBA

```bash
#!/bin/bash

# 1. Obtener token
echo "=== TEST 1: Obtener Token ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass1"}')
TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token obtenido: ${TOKEN:0:50}..."

# 2. Crear orden
echo -e "\n=== TEST 2: Crear Orden ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"customerId":"cust123","productId":"prod456","quantity":5}')
ORDER_ID=$(echo $RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Orden creada con ID: $ORDER_ID"

# 3. Esperar a que se procese
echo -e "\n=== TEST 3: Esperando procesamiento (10s) ==="
sleep 10

# 4. Consultar orden
echo -e "\n=== TEST 4: Consultar Orden ==="
curl -s -X GET "http://localhost:8080/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"status":"[^"]*'

echo -e "\n=== PRUEBAS COMPLETADAS ==="
```

---

## 🚨 Errores Comunes y Soluciones

### Error: "Connection refused" en port 8080
```bash
# Solución: Espera 30s más, el gateway necesita tiempo
sleep 30
curl http://localhost:8080/auth/health
```

### Error: "401 Unauthorized"
```bash
# Solución: El token expiró o es inválido
# Obtén uno nuevo:
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass1"}'
```

### Error: "Bad credentials"
```bash
# Solución: Usuario o contraseña incorrectos
# Usuarios válidos: user1/pass1, user2/pass2, admin/admin123
```

### Error: "No service found"
```bash
# Solución: El servicio aún no se registró en Eureka
# Espera otro 30s y reintenta
```

### RabbitMQ no visible en dashboard
```bash
# Solución: Verifica que Docker está corriendo
docker ps | grep rabbitmq
docker logs rabbitmq
```

---

## 📊 Verificación Final

```bash
# Ejecutar esto después de todas las pruebas

echo "=== ESTADO DEL ECOSISTEMA ==="

echo -n "✓ Eureka: "
curl -s http://localhost:8761/eureka/apps | grep -o 'application>' | wc -l

echo -n "✓ RabbitMQ: "
curl -s -u guest:guest http://localhost:15672/api/overview | grep -o '"queue_totals"' && echo "UP"

echo -n "✓ Order Service: "
curl -s http://localhost:8082/orders/health | grep -o 'UP'

echo -n "✓ Auth Service: "
curl -s http://localhost:8081/auth/health | grep -o 'UP'

echo -n "✓ Inventory Service: "
curl -s http://localhost:8083/inventory/health | grep -o 'UP'

echo "=== FIN DE VERIFICACIÓN ==="
```

---

## 📝 Notas Importantes

1. **Tiempos de inicio:**
   - RabbitMQ: 10s
   - Eureka: 15s
   - Otros servicios: 20-30s

2. **Delays en procesamiento:**
   - Los eventos de RabbitMQ se procesan casi instantáneamente
   - Espera 2-5s después de crear una orden para ver cambios

3. **JWT expira en:**
   - 24 horas (86400000 ms)
   - Si expira, obtén uno nuevo

4. **Logs útiles:**
   - `docker-compose logs -f <service>` para ver logs en vivo
   - `docker logs <container>` para logs históricos

---

**Última actualización:** 2026-06-20
