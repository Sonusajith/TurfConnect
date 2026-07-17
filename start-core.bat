@echo off
echo ========================================================
echo Starting TurfConnect Core Services (Low RAM Mode)
echo ========================================================
echo This script will start ONLY the 5 essential microservices
echo needed for the main booking flow, keeping RAM usage low.
echo Ensure MongoDB, Redis, and RabbitMQ are running.
echo.

cd backend

echo [1/5] Starting API Gateway...
start "API Gateway" cmd /c ".\mvnw.cmd spring-boot:run -pl api-gateway"

echo [2/5] Starting Auth Service...
start "Auth Service" cmd /c ".\mvnw.cmd spring-boot:run -pl auth-service"

echo [3/5] Starting Turf Service...
start "Turf Service" cmd /c ".\mvnw.cmd spring-boot:run -pl turf-service"

echo [4/5] Starting Booking Service...
start "Booking Service" cmd /c ".\mvnw.cmd spring-boot:run -pl booking-service"

echo [5/5] Starting Payment Service...
start "Payment Service" cmd /c ".\mvnw.cmd spring-boot:run -pl payment-service"

echo.
echo All core services are starting in separate windows.
echo Wait about 30 seconds for them to fully initialize.
echo API Gateway will be available at http://localhost:8080
cd ..
pause
