@echo off
setlocal EnableDelayedExpansion

:: ─────────────────────────────────────────────────────────────────────────────
:: MediConnect — Windows Launcher
:: Requires: Docker Desktop for Windows
:: ─────────────────────────────────────────────────────────────────────────────

title MediConnect - Pharmaceutical Shortage Marketplace

echo.
echo   __  __          _ _  ____                            _   
echo  ^|  \/  ^|        ^| (_)/ ___^|                          ^| ^|  
echo  ^| \  / ^| ___  __^| ^|_^| ^|     ___  _ __  _ __   ___  ___^| ^|_ 
echo  ^| ^|\/^| ^|/ _ \/ _` ^| ^| ^|    / _ \^| '_ \^| '_ \ / _ \/ __^| __^|
echo  ^| ^|  ^| ^|  __/ (_^| ^| ^| ^|___^| (_) ^| ^| ^| ^| ^| ^|  __/ (__^| ^|_ 
echo  ^|_^|  ^|_^|\___^|\__,_^|_^|\____^|\___/^|_^| ^|_^|_^| ^|_^|\___^|\___^|\__^|
echo.
echo  Connecting Countries, Delivering Hope
echo  ============================================
echo  Quarkus + gRPC + Angular 21 + PostgreSQL
echo  ============================================
echo.

:: Check Docker is installed
where docker >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Docker not found. Install Docker Desktop from:
    echo         https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

:: Check Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Docker Desktop is not running.
    echo         Please start Docker Desktop and try again.
    pause
    exit /b 1
)

:: Check docker-compose
where docker-compose >nul 2>&1
if %ERRORLEVEL% neq 0 (
    :: Try docker compose (v2 plugin)
    docker compose version >nul 2>&1
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] docker-compose not found.
        echo         Update Docker Desktop to get docker compose v2.
        pause
        exit /b 1
    )
    set COMPOSE=docker compose
) else (
    set COMPOSE=docker-compose
)

:: Default mode
set MODE=%1
if "%MODE%"=="" set MODE=docker

:: ── Docker mode ───────────────────────────────────────────────────────────────
if "%MODE%"=="docker" (
    echo [INFO] Mode: Docker Compose (full stack)
    echo.

    echo [....] Building and starting all services...
    %COMPOSE% up --build -d

    if %ERRORLEVEL% neq 0 (
        echo.
        echo [ERROR] Failed to start services. Check the output above.
        pause
        exit /b 1
    )

    echo.
    echo  ============================================
    echo   MediConnect is starting up!
    echo  ============================================
    echo.
    echo   Frontend    http://localhost:4200
    echo   Backend     http://localhost:8080
    echo   Swagger     http://localhost:8080/swagger-ui
    echo   gRPC        localhost:9000
    echo   PostgreSQL  localhost:5432
    echo.
    echo   Login:  admin@mediconnect.global
    echo   Pass:   Admin123!
    echo.
    echo   Logs:   %COMPOSE% logs -f backend
    echo   Stop:   %COMPOSE% down
    echo.
    echo  ============================================
    echo.

    :: Wait a moment then open browser
    echo [INFO] Opening browser in 10 seconds...
    timeout /t 10 /nobreak >nul
    start http://localhost:4200

    goto :done
)

:: ── Dev mode ──────────────────────────────────────────────────────────────────
if "%MODE%"=="dev" (
    echo [INFO] Mode: Development (hot-reload)
    echo.

    :: Check Java
    where java >nul 2>&1
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Java 21 not found. Install JDK 21 from:
        echo         https://adoptium.net/temurin/releases/?version=21
        pause
        exit /b 1
    )

    :: Check Node
    where node >nul 2>&1
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Node.js not found. Install from:
        echo         https://nodejs.org/
        pause
        exit /b 1
    )

    :: Check npm deps installed
    if not exist frontend\node_modules (
        echo [INFO] Installing frontend dependencies...
        cd frontend && npm install && cd ..
    )

    :: Start PostgreSQL
    echo [....] Starting PostgreSQL...
    %COMPOSE% up -d postgres
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Failed to start PostgreSQL
        pause
        exit /b 1
    )
    echo [ OK ] PostgreSQL started

    :: Generate JWT keys if missing
    if not exist backend\src\main\resources\META-INF\resources\privateKey.pem (
        echo [INFO] Generating JWT keys...
        mkdir backend\src\main\resources\META-INF\resources 2>nul
        cd backend\src\main\resources\META-INF\resources
        openssl genrsa -out privateKey.pem 2048
        openssl rsa -pubout -in privateKey.pem -out publicKey.pem
        cd ..\..\..\..\..\..
        echo [ OK ] JWT keys generated
    )

    :: Start backend in new window
    echo [....] Starting Quarkus backend (new window)...
    start "MediConnect Backend" cmd /k "cd backend && gradlew.bat quarkusDev"

    :: Wait for backend
    echo [INFO] Waiting for backend to start...
    timeout /t 20 /nobreak >nul

    :: Start frontend in new window
    echo [....] Starting Angular frontend (new window)...
    start "MediConnect Frontend" cmd /k "cd frontend && npm start"

    echo.
    echo  ============================================
    echo   Dev mode started in separate windows!
    echo  ============================================
    echo.
    echo   Frontend    http://localhost:4200
    echo   Backend     http://localhost:8080
    echo   Swagger     http://localhost:8080/swagger-ui
    echo   gRPC        localhost:9000
    echo.
    echo   Login:  admin@mediconnect.global
    echo   Pass:   Admin123!
    echo.
    echo   Hot-reload is active in both windows.
    echo   Close the terminal windows to stop.
    echo  ============================================
    echo.

    goto :done
)

:: ── Help ─────────────────────────────────────────────────────────────────────
echo Usage: start.bat [docker^|dev]
echo.
echo   docker   Full Docker Compose stack (default, recommended)
echo   dev      Hot-reload dev mode (requires Java 21 + Node.js)
echo.
echo Examples:
echo   start.bat
echo   start.bat docker
echo   start.bat dev
echo.

:done
endlocal
