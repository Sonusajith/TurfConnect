@echo off
for /f "usebackq tokens=1,* delims==" %%a in (`type ".env" ^| findstr /v "^#" ^| findstr /v "^$"`) do (
    set "%%a=%%~b"
)
cd backend
.\mvnw.cmd spring-boot:run -pl api-gateway
