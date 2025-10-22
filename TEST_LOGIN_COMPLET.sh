#!/bin/bash

# Script de test complet pour l'authentification JWT
# Projet: PIBourse - Spring Boot 3.5.6 + Spring Security 6 + JWT

echo "============================================"
echo "🧪 TEST COMPLET AUTHENTIFICATION JWT"
echo "============================================"
echo ""

BASE_URL="http://localhost:8084"

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les résultats
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

echo "📝 Test 1: Register ADMIN"
echo "----------------------------------------"
REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "admin@test.com",
    "password": "admin123",
    "role": "ROLE_ADMIN"
  }')

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

echo "Code HTTP: $HTTP_CODE"
echo "Réponse: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Register ADMIN réussi"
else
    print_result 1 "Register ADMIN échoué"
fi
echo ""

echo "🔐 Test 2: Login ADMIN"
echo "----------------------------------------"
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "password": "admin123"
  }')

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

echo "Code HTTP: $HTTP_CODE"
echo "Réponse: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Login ADMIN réussi"
    # Extraire le token (nécessite jq)
    if command -v jq &> /dev/null; then
        TOKEN=$(echo "$BODY" | jq -r '.token')
        echo "Token JWT: ${TOKEN:0:50}..."
    else
        echo -e "${YELLOW}⚠️  jq non installé - impossible d'extraire le token${NC}"
        TOKEN=""
    fi
else
    print_result 1 "Login ADMIN échoué"
    TOKEN=""
fi
echo ""

if [ -n "$TOKEN" ]; then
    echo "🔒 Test 3: Endpoint protégé ADMIN avec token"
    echo "----------------------------------------"
    ADMIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/admin/test" \
      -H "Authorization: Bearer $TOKEN")
    
    HTTP_CODE=$(echo "$ADMIN_RESPONSE" | tail -n1)
    BODY=$(echo "$ADMIN_RESPONSE" | sed '$d')
    
    echo "Code HTTP: $HTTP_CODE"
    echo "Réponse: $BODY"
    
    if [ "$HTTP_CODE" = "200" ]; then
        print_result 0 "Accès endpoint ADMIN réussi"
    else
        print_result 1 "Accès endpoint ADMIN échoué"
    fi
    echo ""
fi

echo "❌ Test 4: Login avec mauvais mot de passe"
echo "----------------------------------------"
BAD_LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "password": "wrongpassword"
  }')

HTTP_CODE=$(echo "$BAD_LOGIN_RESPONSE" | tail -n1)
BODY=$(echo "$BAD_LOGIN_RESPONSE" | sed '$d')

echo "Code HTTP: $HTTP_CODE"
echo "Réponse: $BODY"

if [ "$HTTP_CODE" = "401" ]; then
    print_result 0 "Erreur 401 correcte (mauvais mot de passe)"
else
    print_result 1 "Code HTTP incorrect (attendu: 401)"
fi
echo ""

echo "🚫 Test 5: Endpoint protégé SANS token"
echo "----------------------------------------"
NO_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/admin/test")

HTTP_CODE=$(echo "$NO_TOKEN_RESPONSE" | tail -n1)
BODY=$(echo "$NO_TOKEN_RESPONSE" | sed '$d')

echo "Code HTTP: $HTTP_CODE"
echo "Réponse: $BODY"

if [ "$HTTP_CODE" = "401" ]; then
    print_result 0 "Erreur 401 correcte (pas de token)"
else
    print_result 1 "Code HTTP incorrect (attendu: 401)"
fi
echo ""

echo "📝 Test 6: Register PLAYER"
echo "----------------------------------------"
REGISTER_PLAYER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testplayer",
    "email": "player@test.com",
    "password": "player123",
    "role": "ROLE_PLAYER"
  }')

HTTP_CODE=$(echo "$REGISTER_PLAYER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_PLAYER_RESPONSE" | sed '$d')

echo "Code HTTP: $HTTP_CODE"
echo "Réponse: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Register PLAYER réussi"
else
    print_result 1 "Register PLAYER échoué"
fi
echo ""

echo "🔐 Test 7: Login PLAYER"
echo "----------------------------------------"
LOGIN_PLAYER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testplayer",
    "password": "player123"
  }')

HTTP_CODE=$(echo "$LOGIN_PLAYER_RESPONSE" | tail -n1)
BODY=$(echo "$LOGIN_PLAYER_RESPONSE" | sed '$d')

echo "Code HTTP: $HTTP_CODE"
echo "Réponse: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Login PLAYER réussi"
    if command -v jq &> /dev/null; then
        PLAYER_TOKEN=$(echo "$BODY" | jq -r '.token')
    fi
else
    print_result 1 "Login PLAYER échoué"
    PLAYER_TOKEN=""
fi
echo ""

if [ -n "$PLAYER_TOKEN" ]; then
    echo "🚫 Test 8: PLAYER essaie d'accéder endpoint ADMIN"
    echo "----------------------------------------"
    PLAYER_ADMIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/admin/test" \
      -H "Authorization: Bearer $PLAYER_TOKEN")
    
    HTTP_CODE=$(echo "$PLAYER_ADMIN_RESPONSE" | tail -n1)
    BODY=$(echo "$PLAYER_ADMIN_RESPONSE" | sed '$d')
    
    echo "Code HTTP: $HTTP_CODE"
    echo "Réponse: $BODY"
    
    if [ "$HTTP_CODE" = "403" ]; then
        print_result 0 "Erreur 403 correcte (PLAYER sans droits ADMIN)"
    else
        print_result 1 "Code HTTP incorrect (attendu: 403)"
    fi
    echo ""
    
    echo "✅ Test 9: PLAYER accède endpoint PLAYER"
    echo "----------------------------------------"
    PLAYER_PLAYER_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/test" \
      -H "Authorization: Bearer $PLAYER_TOKEN")
    
    HTTP_CODE=$(echo "$PLAYER_PLAYER_RESPONSE" | tail -n1)
    BODY=$(echo "$PLAYER_PLAYER_RESPONSE" | sed '$d')
    
    echo "Code HTTP: $HTTP_CODE"
    echo "Réponse: $BODY"
    
    if [ "$HTTP_CODE" = "200" ]; then
        print_result 0 "Accès endpoint PLAYER réussi"
    else
        print_result 1 "Accès endpoint PLAYER échoué"
    fi
    echo ""
fi

echo "============================================"
echo "🏁 TESTS TERMINÉS"
echo "============================================"
echo ""
echo "💡 Note: Pour utiliser ce script:"
echo "   1. Assurez-vous que l'application est démarrée (mvn spring-boot:run)"
echo "   2. Installez jq pour extraire les tokens: sudo apt-get install jq"
echo "   3. Exécutez: bash TEST_LOGIN_COMPLET.sh"

