#!/bin/bash

# ============================================================
# Script de test complet pour Game Master
# ============================================================
# Ce script teste l'authentification et la création de session
# ============================================================

echo "======================================"
echo "🚀 TEST COMPLET - MODULE GAME MASTER"
echo "======================================"
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8084"

# ============================================================
# ÉTAPE 1 : Se connecter et obtenir le token
# ============================================================

echo "📝 ÉTAPE 1 : Connexion..."
echo "Endpoint: POST $BASE_URL/api/auth/login"
echo ""

# Demander les credentials
read -p "Email (default: admin@test.com): " email
email=${email:-admin@test.com}

read -sp "Password (default: Password123!): " password
password=${password:-Password123!}
echo ""
echo ""

# Se connecter
echo "⏳ Connexion en cours..."
LOGIN_RESPONSE=$(curl -s -X 'POST' \
  "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{
  \"username\": \"$email\",
  \"password\": \"$password\"
}")

# Vérifier si la connexion a réussi
if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo -e "${GREEN}✅ Connexion réussie !${NC}"
    echo ""
    
    # Extraire le token
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}❌ Erreur : Token non trouvé dans la réponse${NC}"
        echo "Réponse complète :"
        echo "$LOGIN_RESPONSE" | jq '.'
        exit 1
    fi
    
    echo "📋 Informations du compte :"
    echo "$LOGIN_RESPONSE" | jq '{username, email, role}'
    echo ""
    
    echo "🔑 Token JWT (tronqué) :"
    echo "${TOKEN:0:50}..."
    echo ""
else
    echo -e "${RED}❌ Erreur de connexion !${NC}"
    echo "Réponse du serveur :"
    echo "$LOGIN_RESPONSE" | jq '.'
    echo ""
    echo "Vérifiez que :"
    echo "  - L'application est démarrée"
    echo "  - Les credentials sont corrects"
    echo "  - L'utilisateur existe en base de données"
    exit 1
fi

# ============================================================
# ÉTAPE 2 : Créer une session de jeu
# ============================================================

echo "======================================"
echo "📝 ÉTAPE 2 : Création d'une session"
echo "======================================"
echo ""
echo "Endpoint: POST $BASE_URL/api/game-master/sessions"
echo ""

# Date de début (maintenant + 5 jours)
START_DATE=$(date -u -d "+5 days" +"%Y-%m-%dT%H:%M:%S")
# Date de fin (maintenant + 12 jours)
END_DATE=$(date -u -d "+12 days" +"%Y-%m-%dT%H:%M:%S")

echo "⏳ Création de la session..."
SESSION_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X 'POST' \
  "$BASE_URL/api/game-master/sessions" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{
  \"name\": \"Session Bourse 2025\",
  \"description\": \"Simulateur boursier pour le cours de 2025\",
  \"initialBalance\": 10000.0,
  \"currency\": \"USD\",
  \"startDate\": \"$START_DATE\",
  \"endDate\": \"$END_DATE\",
  \"maxPlayers\": 4,
  \"allowLateJoin\": true
}")

# Extraire le code HTTP
HTTP_STATUS=$(echo "$SESSION_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
SESSION_BODY=$(echo "$SESSION_RESPONSE" | sed '/HTTP_STATUS/d')

echo ""
echo "📊 Résultat :"
echo "Code HTTP: $HTTP_STATUS"
echo ""

if [ "$HTTP_STATUS" == "201" ]; then
    echo -e "${GREEN}✅ SUCCESS ! Session créée avec succès !${NC}"
    echo ""
    echo "📋 Détails de la session :"
    echo "$SESSION_BODY" | jq '{id, name, status, initialBalance, currency, playerCount, maxPlayers}'
    echo ""
    
    SESSION_ID=$(echo "$SESSION_BODY" | jq -r '.id')
    echo -e "${GREEN}🎉 Session ID: $SESSION_ID${NC}"
    echo ""
    
    # Sauvegarder les infos pour utilisation future
    echo "Pour utiliser cette session :"
    echo "  Session ID: $SESSION_ID"
    echo "  Token: ${TOKEN:0:50}..."
    
elif [ "$HTTP_STATUS" == "401" ]; then
    echo -e "${RED}❌ ERREUR 401 - Non autorisé${NC}"
    echo ""
    echo "Le token JWT n'est pas accepté."
    echo ""
    echo "Réponse du serveur :"
    echo "$SESSION_BODY" | jq '.'
    echo ""
    echo "Vérifications :"
    echo "  1. Le token est-il valide ?"
    echo "  2. L'utilisateur a-t-il le rôle ADMIN ou GAME_MASTER ?"
    echo "  3. L'application a-t-elle été redémarrée après les modifications ?"
    
elif [ "$HTTP_STATUS" == "403" ]; then
    echo -e "${RED}❌ ERREUR 403 - Accès refusé${NC}"
    echo ""
    echo "Votre utilisateur n'a pas les permissions nécessaires."
    echo ""
    echo "Réponse du serveur :"
    echo "$SESSION_BODY" | jq '.'
    echo ""
    echo "Solution :"
    echo "  UPDATE players SET role = 'ROLE_ADMIN' WHERE email = '$email';"
    
elif [ "$HTTP_STATUS" == "400" ]; then
    echo -e "${RED}❌ ERREUR 400 - Données invalides${NC}"
    echo ""
    echo "Réponse du serveur :"
    echo "$SESSION_BODY" | jq '.'
    
else
    echo -e "${RED}❌ ERREUR HTTP $HTTP_STATUS${NC}"
    echo ""
    echo "Réponse du serveur :"
    echo "$SESSION_BODY" | jq '.'
fi

echo ""
echo "======================================"
echo "🏁 FIN DU TEST"
echo "======================================"


