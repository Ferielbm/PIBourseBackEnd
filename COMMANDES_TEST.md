# 🚀 Commandes de test - À exécuter maintenant

---

## ⚡ Test rapide (2 minutes)

### 1️⃣ Compiler le projet
```bash
mvn clean compile
```
**Résultat attendu** : `BUILD SUCCESS`

---

### 2️⃣ Démarrer l'application
```bash
mvn spring-boot:run
```

**Attendez de voir dans les logs** :
```
Started PiBourseBackEndApplication in X.XXX seconds
```

---

### 3️⃣ Tester Swagger (dans un nouveau terminal)

#### Test A : API Docs
```bash
curl http://localhost:8084/v3/api-docs
```
**✅ Résultat attendu** : JSON contenant `"openapi":"3.0.1"` (PAS d'erreur 500)

#### Test B : Swagger UI
Ouvrir dans le navigateur :
```
http://localhost:8084/swagger-ui.html
```
**✅ Résultat attendu** : Interface Swagger avec tous les endpoints

---

### 4️⃣ Tester l'API

#### Test login
```bash
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Test connexion
```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Test mot de passe oublié
```bash
curl -X POST http://localhost:8084/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

---

## 🧪 Test avec base de données H2 (Mode test)

### 1️⃣ Démarrer avec profil test
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### 2️⃣ Accéder à la console H2
```
http://localhost:8084/h2-console
```

**Paramètres de connexion** :
- JDBC URL: `jdbc:h2:mem:pibourse_test`
- Username: `sa`
- Password: (laisser vide)

### 3️⃣ Vérifier les données
```sql
SELECT * FROM players;
```

---

## 📦 Test avec base MySQL (Mode production)

### 1️⃣ Créer la base de données
```bash
mysql -u root -p < database-test-setup.sql
```

### 2️⃣ Modifier application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pibourse_test?createDatabaseIfNotExist=true
```

### 3️⃣ Démarrer
```bash
mvn spring-boot:run
```

---

## 🔍 Vérification complète

### Checklist

Exécutez ces commandes une par une :

```bash
# 1. Compilation
mvn clean compile
# ✅ Doit afficher "BUILD SUCCESS"

# 2. Tests unitaires (optionnel)
mvn test
# ✅ Peut avoir des erreurs si pas de tests créés (normal)

# 3. Package
mvn package -DskipTests
# ✅ Crée le fichier JAR

# 4. Démarrage
mvn spring-boot:run
# ✅ Application démarre sur port 8084

# Dans un autre terminal :

# 5. Test Swagger
curl -I http://localhost:8084/v3/api-docs
# ✅ Doit retourner HTTP 200

# 6. Test Swagger UI
curl -I http://localhost:8084/swagger-ui.html
# ✅ Doit retourner HTTP 200

# 7. Test endpoint public
curl http://localhost:8084/api/auth/login
# ✅ Peut retourner erreur 400 (normal, pas de body) mais pas 404 ou 500
```

---

## 🎯 Tests via fichiers .http

### VS Code / IntelliJ

Ouvrez et exécutez :

1. **API_EXAMPLES_PASSWORD_RESET.http**
   - Testez le flow complet de reset password

2. **API_EXAMPLES.http** (si existant)
   - Testez les autres endpoints

---

## 📊 Résultats attendus

### ✅ Swagger UI
![Swagger UI accessible](http://localhost:8084/swagger-ui.html)

**Doit afficher** :
- Authentication (5 endpoints)
- Player Management (plusieurs endpoints)
- Autres modules

### ✅ API Docs JSON
```json
{
  "openapi": "3.0.1",
  "info": {
    "title": "PiBourse API - Trading Platform",
    "version": "2.0.0"
  },
  "servers": [
    {
      "url": "http://localhost:8084",
      "description": "Local Development Server"
    }
  ],
  "paths": {
    "/api/auth/login": {...},
    "/api/auth/register": {...},
    "/api/auth/forgot-password": {...},
    ...
  }
}
```

---

## 🚨 Si erreur 500 persiste

### Diagnostic complet

```bash
# 1. Supprimer target/
rm -rf target/         # Linux/Mac
rmdir /s /q target     # Windows

# 2. Clean install
mvn clean install -DskipTests

# 3. Vérifier les dépendances
mvn dependency:tree | grep springdoc

# 4. Démarrer en mode debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug"

# 5. Logs détaillés
# Modifier application.properties :
# logging.level.org.springdoc=DEBUG
# logging.level.org.springframework.web=DEBUG
```

### Tester endpoint par endpoint

```bash
# Test 1 : Health check (si activé)
curl http://localhost:8084/actuator/health

# Test 2 : API docs
curl http://localhost:8084/v3/api-docs

# Test 3 : Swagger UI
curl http://localhost:8084/swagger-ui.html

# Test 4 : Auth endpoints
curl http://localhost:8084/api/auth/login

# Vérifier les logs après chaque requête
```

---

## 📱 Test depuis Postman

### Import de la collection

1. Créer une nouvelle collection "PiBourse API"
2. Ajouter les endpoints :

#### Environnement
```
baseUrl = http://localhost:8084
```

#### Requests
```
POST {{baseUrl}}/api/auth/register
POST {{baseUrl}}/api/auth/login
POST {{baseUrl}}/api/auth/forgot-password
GET  {{baseUrl}}/api/auth/validate-reset?token={{token}}
POST {{baseUrl}}/api/auth/reset-password
```

---

## ✅ Validation finale

Tout fonctionne si :

- [x] `mvn clean compile` → BUILD SUCCESS
- [ ] `mvn spring-boot:run` → Application démarre
- [ ] `curl http://localhost:8084/v3/api-docs` → HTTP 200
- [ ] http://localhost:8084/swagger-ui.html → Interface visible
- [ ] Login fonctionne
- [ ] Mot de passe oublié fonctionne
- [ ] H2 console accessible (en mode test)

---

## 🎉 Commandes finales

### Développement normal
```bash
mvn spring-boot:run
```

### Mode test (H2)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### Build pour production
```bash
mvn clean package -DskipTests
java -jar target/PIBourseBackEnd-0.0.1-SNAPSHOT.jar
```

---

**C'est parti ! Testez maintenant ! 🚀**

Commencez par :
```bash
mvn clean spring-boot:run
```

Puis ouvrez : **http://localhost:8084/swagger-ui.html**

