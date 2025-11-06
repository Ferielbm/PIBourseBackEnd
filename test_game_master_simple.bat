@echo off
REM ============================================================
REM Script de test simple pour Windows - Module Game Master
REM ============================================================

echo ======================================
echo TEST COMPLET - MODULE GAME MASTER
echo ======================================
echo.

set BASE_URL=http://localhost:8084

REM ============================================================
REM ETAPE 1 : Connexion
REM ============================================================

echo ETAPE 1 : Connexion...
echo.

set /p EMAIL="Email (default: admin@test.com): "
if "%EMAIL%"=="" set EMAIL=admin@test.com

set /p PASSWORD="Password (default: Password123!): "
if "%PASSWORD%"=="" set PASSWORD=Password123!

echo.
echo Connexion en cours...

curl -s -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"%EMAIL%\",\"password\":\"%PASSWORD%\"}" ^
  > login_response.json

echo.
echo Reponse de connexion :
type login_response.json
echo.
echo.

echo Copiez le token de la reponse ci-dessus
set /p TOKEN="Collez le token JWT ici : "

echo.

REM ============================================================
REM ETAPE 2 : Creation de session
REM ============================================================

echo ======================================
echo ETAPE 2 : Creation d'une session
echo ======================================
echo.

curl -X POST "%BASE_URL%/api/game-master/sessions" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Session Bourse 2025\",\"description\":\"Simulateur boursier\",\"initialBalance\":10000.0,\"currency\":\"USD\",\"startDate\":\"2025-11-10T09:00:00\",\"endDate\":\"2025-11-17T18:00:00\",\"maxPlayers\":4,\"allowLateJoin\":true}" ^
  > session_response.json

echo.
echo Reponse de creation de session :
type session_response.json
echo.

del login_response.json
del session_response.json

echo.
echo ======================================
echo FIN DU TEST
echo ======================================
pause


