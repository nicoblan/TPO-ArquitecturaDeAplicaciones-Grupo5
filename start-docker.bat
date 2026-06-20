@echo off
REM Script para levantar el ecosistema de microservicios con Docker Compose

echo ===============================================
echo Iniciando ecosistema de microservicios
echo ===============================================

echo.
echo 1. Verificando Docker...
docker --version
if %errorlevel% neq 0 (
    echo ERROR: Docker no está instalado o no está en PATH
    pause
    exit /b 1
)

echo.
echo 2. Construyendo e iniciando servicios con Docker Compose...
docker-compose up --build

echo.
echo 3. Servicios levantados correctamente!
echo.
echo URLs importantes:
echo   - Eureka: http://localhost:8761
echo   - Gateway: http://localhost:8080
echo   - Auth: http://localhost:8081
echo   - Order: http://localhost:8082
echo   - Inventory: http://localhost:8083
echo   - Notification: http://localhost:8084
echo   - RabbitMQ Management: http://localhost:15672 (guest/guest)
echo.
echo Presiona CTRL+C para detener los servicios...
pause
