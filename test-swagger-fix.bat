@echo off
echo ========================================
echo Test Swagger - Fix erreur 500
echo ========================================
echo.

echo 1. Demarrage de l'application...
echo    (Attendez que l'application demarre completement)
echo.
start /B mvn spring-boot:run

echo 2. Attente du demarrage (30 secondes)...
timeout /t 30 /nobreak >nul

echo.
echo 3. Test de l'endpoint /v3/api-docs
echo ----------------------------------------
curl -s http://localhost:8084/v3/api-docs > test-result.json

if %ERRORLEVEL% EQU 0 (
    echo ✅ SUCCESS - Endpoint accessible
    echo.
    echo Contenu du fichier test-result.json :
    type test-result.json | findstr /C:"openapi" >nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ SUCCESS - Documentation OpenAPI generee correctement
    ) else (
        echo ❌ ERREUR - Le fichier ne contient pas de documentation OpenAPI valide
    )
) else (
    echo ❌ ERREUR - Impossible d'acceder a l'endpoint
)

echo.
echo 4. Test de Swagger UI
echo ----------------------------------------
curl -s -o swagger-ui.html http://localhost:8084/swagger-ui.html
if %ERRORLEVEL% EQU 0 (
    echo ✅ SUCCESS - Swagger UI accessible sur http://localhost:8084/swagger-ui.html
) else (
    echo ❌ ERREUR - Swagger UI non accessible
)

echo.
echo ========================================
echo Tests termines
echo ========================================
echo.
echo Ouvrez votre navigateur :
echo - Swagger UI : http://localhost:8084/swagger-ui.html
echo - API Docs   : http://localhost:8084/v3/api-docs
echo.
pause

