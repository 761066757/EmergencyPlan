@echo off
setlocal

:: =============================================
:: Emergency Platform Deploy Script
:: Usage: deploy.bat <version>
:: Example: deploy.bat v1.0.0
:: Rollback: deploy.bat v0.9.0
:: =============================================

set VERSION=%1

if "%VERSION%"=="" (
    echo [ERROR] Please specify a version
    echo Usage: deploy.bat ^<version^>
    echo Example: deploy.bat v1.0.0
    echo.
    echo Existing images:
    docker images emergencyplan-server --format "table {{.Tag}}\t{{.CreatedAt}}\t{{.Size}}"
    exit /b 1
)

echo ============================================
echo   Emergency Platform Deploy - %VERSION%
echo ============================================

:: Check if image already exists (rollback scenario)
docker image inspect emergencyplan-server:%VERSION% >nul 2>&1
if %errorlevel%==0 (
    echo [ROLLBACK] Found existing image emergencyplan-server:%VERSION%, starting...
    set APP_VERSION=%VERSION%
    docker compose up -d
    echo.
    echo [DONE] Rolled back to %VERSION%
    docker compose ps
    exit /b 0
)

:: Build new image
echo [BUILD] Building new image...
set APP_VERSION=%VERSION%
docker compose build
if %errorlevel% neq 0 (
    echo [ERROR] Build failed
    exit /b 1
)

:: Tag image
echo [TAG] Tagging image %VERSION% ...
docker tag emergencyplan-server:%VERSION% emergencyplan-server:latest

:: Start services
echo [START] Starting services...
docker compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Start failed
    exit /b 1
)

:: Health check
echo [CHECK] Waiting for services to start...
timeout /t 15 /nobreak >nul

:: Show results
echo.
echo ============================================
echo   Deploy Complete
echo ============================================
docker compose ps
echo.
echo Image versions:
docker images emergencyplan-server --format "table {{.Tag}}\t{{.CreatedAt}}\t{{.Size}}"
echo.
echo Rollback: deploy.bat ^<old-version^>

:: Push to Docker Hub
echo.
echo [PUSH] Pushing to Docker Hub...
docker tag emergencyplan-server:%VERSION% sp1kela/emergencyplan-server:latest
docker push sp1kela/emergencyplan-server:latest
if %errorlevel%==0 (
    echo [PUSH] Pushed to sp1kela/emergencyplan-server:latest
) else (
    echo [WARN] Push failed, image is still available locally
)

endlocal
