#!/bin/bash

echo "========================================"
echo "Test Swagger - Fix erreur 500"
echo "========================================"
echo ""

echo "1. Démarrage de l'application..."
echo "   (Attendez que l'application démarre complètement)"
echo ""

# Démarrer l'application en arrière-plan
mvn spring-boot:run > app.log 2>&1 &
APP_PID=$!

echo "2. Attente du démarrage (30 secondes)..."
sleep 30

echo ""
echo "3. Test de l'endpoint /v3/api-docs"
echo "----------------------------------------"

HTTP_CODE=$(curl -s -o test-result.json -w "%{http_code}" http://localhost:8084/v3/api-docs)

if [ "$HTTP_CODE" == "200" ]; then
    echo "✅ SUCCESS - Endpoint accessible (HTTP $HTTP_CODE)"
    echo ""
    
    if grep -q "openapi" test-result.json; then
        echo "✅ SUCCESS - Documentation OpenAPI générée correctement"
        echo ""
        echo "Aperçu :"
        head -20 test-result.json
    else
        echo "❌ ERREUR - Le fichier ne contient pas de documentation OpenAPI valide"
    fi
else
    echo "❌ ERREUR - Code HTTP: $HTTP_CODE"
    cat test-result.json
fi

echo ""
echo "4. Test de Swagger UI"
echo "----------------------------------------"

HTTP_CODE=$(curl -s -o swagger-ui.html -w "%{http_code}" http://localhost:8084/swagger-ui.html)

if [ "$HTTP_CODE" == "200" ]; then
    echo "✅ SUCCESS - Swagger UI accessible (HTTP $HTTP_CODE)"
else
    echo "❌ ERREUR - Swagger UI non accessible (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================"
echo "Tests terminés"
echo "========================================"
echo ""
echo "Ouvrez votre navigateur :"
echo "- Swagger UI : http://localhost:8084/swagger-ui.html"
echo "- API Docs   : http://localhost:8084/v3/api-docs"
echo ""
echo "Pour arrêter l'application : kill $APP_PID"
echo ""

