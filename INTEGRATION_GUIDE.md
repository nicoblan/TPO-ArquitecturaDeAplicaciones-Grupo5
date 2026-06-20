# 🚀 Guía de Integración del Ecosistema de Microservicios

## 📋 Estructura Creada

Se han creado los siguientes servicios:

### Infraestructura
- **RabbitMQ** (puerto 5672, management 15672)

### Core Services
- **Eureka Server** (puerto 8761) - Service Registry
- **API Gateway** (puerto 8080) - Enrutador de solicitudes
- **Config Server** (puerto 8888) - Configuración centralizada

### Microservicios
- **Auth Service** (puerto 8081) - Autenticación con JWT
- **Order Service** (puerto 8082) - Gestión de órdenes (ya existente)
- **Inventory Service** (puerto 8083) - Gestión de inventario
- **Notification Service** (puerto 8084) - Notificaciones

---

## 🔧 Opción 1: Ejecución con Docker Compose (Recomendado)

### Requisitos
- Docker Desktop instalado y corriendo
- Docker Compose instalado

### Pasos

1. **Navega al directorio raíz del proyecto**
```bash
cd C:\Users\santi\OneDrive\Escritorio\ARQ_APPS\TPO-ArquitecturaDeAplicaciones-Grupo5
```

2. **Construye y levanta los servicios**
```bash
docker-compose up --build
```

3. **Verifica el estado**
   - Eureka: http://localhost:8761
   - RabbitMQ: http://localhost:15672 (usuario: guest, pass: guest)
   - Gateway: http://localhost:8080

### Detener servicios
```bash
docker-compose down
```

---

## 🔧 Opción 2: Ejecución Manual con Maven (Para desarrollo local)

### Requisitos
- Java 17 instalado
- Maven 3.8+ instalado
- RabbitMQ corriendo en localhost:5672

### Pasos (Ejecutar en orden en diferentes terminales)

1. **Inicia RabbitMQ (si no está en Docker)**
```bash
# Asume RabbitMQ instalado localmente
rabbitmq-server
```

2. **Inicia Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
```

3. **Inicia Auth Service** (nueva terminal)
```bash
cd auth-service
mvn spring-boot:run
```

4. **Inicia API Gateway** (nueva terminal)
```bash
cd api-gateway
mvn spring-boot:run
```

5. **Inicia Order Service** (nueva terminal)
```bash
cd order-service
mvn spring-boot:run
```

6. **Inicia Inventory Service** (nueva terminal)
```bash
cd inventory-service
mvn spring-boot:run
```

7. **Inicia Notification Service** (nueva terminal)
```bash
cd notification-service
mvn spring-boot:run
```

---

## 🧪 Pruebas del Ecosistema

### 1. Verificar registros en Eureka
```
GET http://localhost:8761/eureka/apps
```

### 2. Obtener Token JWT
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass1"}'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user1",
  "expiresIn": "86400"
}
```

### 3. Crear una Orden (con JWT)
```bash
# Reemplaza TOKEN con el token obtenido
curl -X POST http://localhost:8080/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "customerId": "cust123",
    "productId": "prod456",
    "quantity": 5
  }'
```

### 4. Consultar Orden
```bash
curl -X GET http://localhost:8080/orders/123 \
  -H "Authorization: Bearer TOKEN"
```

### 5. Verificar Inventario
```bash
curl -X GET http://localhost:8080/inventory/prod456
```

### 6. Monitorear RabbitMQ
- Accede a: http://localhost:15672
- Usuario: guest
- Contraseña: guest
- Verifica que se crean los exchanges: `order.events`, `inventory.events`

---

## 📡 Flujo de Integración

### Saga Asíncrona: Crear Orden

```
Cliente
  ↓
[POST /orders/create + JWT] 
  ↓
Order Service (crea orden, estado PENDING)
  ↓
Publica: order.created
  ↓
  ├→ Inventory Service (consume order.created)
  │   ├→ Reserva inventario
  │   └→ Publica: inventory.updated
  │       ↓
  │       Order Service (consume inventory.updated)
  │       └→ Actualiza orden a CONFIRMED
  │
  └→ Notification Service (consume order.created)
      └→ Simula envío de notificación
```

---

## 🔑 Credenciales de Prueba

### Auth Service
| Usuario | Contraseña |
|---------|-----------|
| user1   | pass1     |
| user2   | pass2     |
| admin   | admin123  |

### RabbitMQ
| Usuario | Contraseña |
|---------|-----------|
| guest   | guest      |

---

## 📊 Puertos Mapeados

| Servicio | Puerto | Función |
|----------|--------|---------|
| Eureka | 8761 | Service Registry |
| Gateway | 8080 | API Gateway |
| Auth | 8081 | Autenticación |
| Order | 8082 | Gestión de órdenes |
| Inventory | 8083 | Gestión de inventario |
| Notification | 8084 | Notificaciones |
| Config | 8888 | Configuración |
| RabbitMQ | 5672 | Message Broker |
| RabbitMQ Mgmt | 15672 | Management UI |

---

## 🐛 Troubleshooting

### Error: "Connection refused" en RabbitMQ
**Solución:** Asegúrate de que RabbitMQ está corriendo
```bash
# Docker
docker ps | grep rabbitmq

# Local
lsof -i :5672
```

### Error: "Eureka not reachable"
**Solución:** Espera 30 segundos después de iniciar Eureka, los clientes se registran automáticamente

### Error: "Port already in use"
**Solución:** Mata el proceso que usa el puerto
```bash
# En Windows (PowerShell)
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process

# En Linux/Mac
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

### Logs de un servicio específico
```bash
# Con Docker
docker logs -f order-service

# Con Maven
mvn spring-boot:run | grep -i "order\|error\|warn"
```

---

## ✅ Checklist de Validación

- [ ] RabbitMQ accesible en http://localhost:15672
- [ ] Eureka accesible en http://localhost:8761
- [ ] Todos los servicios registrados en Eureka
- [ ] Obtener token JWT desde Auth Service
- [ ] Crear orden a través de Gateway con JWT
- [ ] Verificar que order.created se publica en RabbitMQ
- [ ] Verificar que inventory-service consume order.created
- [ ] Verificar que inventory.updated se publica
- [ ] Verificar que notification-service consume eventos
- [ ] Logs muestran transacciones completadas

---

## 📚 Próximos Pasos (Punto 2)

Una vez validado el ecosistema integrado, proceder a:
1. Crear SAD (Solution Architecture Document) de 15-25 páginas
2. Documentar C1, C2, C3 diagrams (C4 Model)
3. Generar evidencias/capturas de pantalla del PoC funcionando

---

**Última actualización:** 2026-06-20
**Estado:** ✅ Ecosistema integrado y listo para testear
