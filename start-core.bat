@echo off
echo ========================================================
echo Starting TurfConnect Core Services (Low RAM Mode)
echo ========================================================
echo Loading environment variables from .env...
for /f "usebackq tokens=1,* delims==" %%a in (`type ".env" ^| findstr /v "^#" ^| findstr /v "^$"`) do (
    set "%%a=%%~b"
)
echo Environment variables loaded!
echo.
cd backend

echo [1/7] Starting API Gateway...
start "API Gateway" cmd /k ".\mvnw.cmd spring-boot:run -pl api-gateway"

echo [2/7] Starting Auth Service...
start "Auth Service" cmd /k ".\mvnw.cmd spring-boot:run -pl auth-service"

echo [3/7] Starting Turf Service...
start "Turf Service" cmd /k ".\mvnw.cmd spring-boot:run -pl turf-service"

echo [4/7] Starting Booking Service...
start "Booking Service" cmd /k ".\mvnw.cmd spring-boot:run -pl booking-service"

echo [5/7] Starting Payment Service...
start "Payment Service" cmd /k ".\mvnw.cmd spring-boot:run -pl payment-service"

echo [6/7] Starting Review Service...
start "Review Service" cmd /k ".\mvnw.cmd spring-boot:run -pl review-service"

echo [7/7] Starting Community Service...
start "Community Service" cmd /k ".\mvnw.cmd spring-boot:run -pl community-service"

echo.
echo All core services are starting in separate windows.
echo Wait about 45 seconds for them to fully initialize.
echo API Gateway will be available at http://localhost:8080
cd ..
pause
