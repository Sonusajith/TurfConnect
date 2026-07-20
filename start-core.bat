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

echo [1/5] Starting API Gateway...
start "API Gateway" cmd /k ".\mvnw.cmd spring-boot:run -pl api-gateway"

echo [2/5] Starting Auth Service...
start "Auth Service" cmd /k ".\mvnw.cmd spring-boot:run -pl auth-service"

echo [3/5] Starting Turf Service...
start "Turf Service" cmd /k ".\mvnw.cmd spring-boot:run -pl turf-service"

echo [4/5] Starting Booking Service...
start "Booking Service" cmd /k ".\mvnw.cmd spring-boot:run -pl booking-service"

echo [5/5] Starting Payment Service...
start "Payment Service" cmd /k ".\mvnw.cmd spring-boot:run -pl payment-service"

echo.
echo All core services are starting in separate windows.
echo Wait about 30 seconds for them to fully initialize.
echo API Gateway will be available at http://localhost:8080
cd ..
pause
