# 🚀 Guide de Démarrage Rapide - PiBourse Player & Transaction API

## ⚡ Démarrage en 5 Minutes

### Prérequis

Avant de commencer, assurez-vous d'avoir :

- ✅ **Java 17+** installé
  ```bash
  java -version
  # Devrait afficher : java version "17.x.x" ou supérieur
  ```

- ✅ **MySQL 8.0+** installé et en cours d'exécution
  ```bash
  # Windows: Services → MySQL → Démarré
  # Linux/Mac: sudo systemctl status mysql
  ```

- ✅ **Maven 3.6+** (ou utiliser le wrapper Maven inclus)
  ```bash
  mvn -version
  # Ou utiliser ./mvnw (Linux/Mac) ou mvnw.cmd (Windows)
  ```

### 📋 Étape 1 : Configurer la Base de Données

**Option A : Configuration par défaut**

Si votre MySQL utilise :
- Port : `3306`
- Username : `root`
- Password : (vide)

→ **Rien à faire !** La base de données `pibourse` sera créée automatiquement.

**Option B : Configuration personnalisée**

Modifiez `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pibourse?createDatabaseIfNotExist=true
spring.datasource.username=votre_username
spring.datasource.password=votre_password
```

### 🔧 Étape 2 : Installer les Dépendances

```bash
# Avec Maven installé
mvn clean install

# Ou avec Maven Wrapper (recommandé)
# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

### ▶️ Étape 3 : Lancer l'Application

```bash
# Avec Maven
mvn spring-boot:run

# Ou avec Maven Wrapper
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Vous devriez voir :**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.5.6)

...
INFO - Initializing database with test data...
INFO - Created 4 players
INFO - Created 10 transactions
INFO - Database initialization completed successfully!
INFO - Started PiBourseBackEndApplication in 5.234 seconds
```

### 🌐 Étape 4 : Accéder à Swagger UI

Ouvrez votre navigateur et allez sur :

```
http://localhost:8084/swagger-ui.html
```

Vous devriez voir l'interface Swagger avec deux sections :
- 🎮 **Player Management**
- 💰 **Transaction Management**

### ✅ Étape 5 : Tester l'API

#### Test 1 : Récupérer tous les joueurs

Dans Swagger UI :
1. Cliquez sur **Player Management**
2. Cliquez sur **GET /api/players**
3. Cliquez sur **"Try it out"**
4. Cliquez sur **"Execute"**

**Résultat attendu :** Liste de 4 joueurs (admin, john_trader, sarah_investor, mike_stocks)

#### Test 2 : Créer un nouveau joueur

1. Cliquez sur **POST /api/players**
2. Cliquez sur **"Try it out"**
3. Remplacez le JSON par :
```json
{
  "username": "test_player",
  "email": "test@example.com",
  "password": "password123",
  "role": "ROLE_PLAYER"
}
```
4. Cliquez sur **"Execute"**

**Résultat attendu :** Code 201 Created avec les détails du joueur créé

#### Test 3 : Créer une transaction

1. Cliquez sur **Transaction Management**
2. Cliquez sur **POST /api/transactions**
3. Cliquez sur **"Try it out"**
4. Utilisez ce JSON :
```json
{
  "type": "DEPOSIT",
  "amount": 1000.0,
  "playerId": 1
}
```
5. Cliquez sur **"Execute"**

**Résultat attendu :** Code 201 Created avec les détails de la transaction

#### Test 4 : Récupérer les transactions d'un joueur

1. Cliquez sur **GET /api/transactions/player/{playerId}**
2. Cliquez sur **"Try it out"**
3. Entrez `1` pour playerId
4. Cliquez sur **"Execute"**

**Résultat attendu :** Liste des transactions du joueur avec ID 1

## 🎯 Données de Test Pré-chargées

L'application charge automatiquement ces données au démarrage :

### Joueurs

| ID | Username | Email | Password | Role |
|----|----------|-------|----------|------|
| 1 | admin | admin@pibourse.tn | admin123 | ROLE_ADMIN |
| 2 | john_trader | john@example.com | password123 | ROLE_PLAYER |
| 3 | sarah_investor | sarah@example.com | password123 | ROLE_PLAYER |
| 4 | mike_stocks | mike@example.com | password123 | ROLE_PLAYER |

### Transactions

- 10 transactions réparties entre les joueurs
- Types : DEPOSIT, BUY, SELL, WITHDRAW
- Montants variés entre 2000€ et 20000€

## 🔍 Tests Supplémentaires

### Test des Validations

**Email invalide (devrait retourner 400) :**
```json
POST /api/players
{
  "username": "test",
  "email": "invalid-email",
  "password": "password123",
  "role": "ROLE_PLAYER"
}
```

**Mot de passe trop court (devrait retourner 400) :**
```json
POST /api/players
{
  "username": "test",
  "email": "test@example.com",
  "password": "123",
  "role": "ROLE_PLAYER"
}
```

**Montant négatif (devrait retourner 400) :**
```json
POST /api/transactions
{
  "type": "DEPOSIT",
  "amount": -100.0,
  "playerId": 1
}
```

**Joueur inexistant (devrait retourner 404) :**
```json
POST /api/transactions
{
  "type": "DEPOSIT",
  "amount": 100.0,
  "playerId": 9999
}
```

## 📊 Tous les Endpoints Disponibles

### Player Management

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/players` | Liste tous les joueurs |
| GET | `/api/players/{id}` | Joueur par ID |
| GET | `/api/players/email/{email}` | Joueur par email |
| POST | `/api/players` | Créer un joueur |
| PUT | `/api/players/{id}` | Mettre à jour un joueur |
| DELETE | `/api/players/{id}` | Supprimer un joueur |

### Transaction Management

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/transactions` | Liste toutes les transactions |
| GET | `/api/transactions/{id}` | Transaction par ID |
| GET | `/api/transactions/player/{playerId}` | Transactions d'un joueur |
| POST | `/api/transactions` | Créer une transaction |
| DELETE | `/api/transactions/{id}` | Supprimer une transaction |

## 🛠️ Résolution de Problèmes

### Problème : "Port 8084 already in use"

**Solution :**
```properties
# Dans application.properties, changez le port
server.port=8085
```

### Problème : "Access denied for user 'root'@'localhost'"

**Solution :**
```properties
# Vérifiez vos identifiants MySQL dans application.properties
spring.datasource.username=votre_username
spring.datasource.password=votre_password
```

### Problème : "Table 'pibourse.players' doesn't exist"

**Solution :**
1. Vérifiez que MySQL est démarré
2. Vérifiez `spring.jpa.hibernate.ddl-auto=update` dans application.properties
3. Supprimez la base `pibourse` et relancez l'application

```sql
DROP DATABASE IF EXISTS pibourse;
```

### Problème : Swagger UI ne s'affiche pas

**Solution :**
1. Vérifiez l'URL : `http://localhost:8084/swagger-ui.html`
2. Vérifiez les logs pour les erreurs
3. Vérifiez la dépendance `springdoc-openapi-starter-webmvc-ui` dans pom.xml

### Problème : Données de test non chargées

**Solution :**
Les données se chargent uniquement si la base est vide. Pour recharger :

```sql
USE pibourse;
DELETE FROM transactions;
DELETE FROM players;
```

Puis relancez l'application.

## 📱 Utiliser l'API avec d'autres outils

### Postman

1. Importez la collection depuis `API_EXAMPLES.http`
2. Base URL : `http://localhost:8084`
3. Testez les endpoints

### cURL

```bash
# Get all players
curl -X GET http://localhost:8084/api/players

# Create a player
curl -X POST http://localhost:8084/api/players \
  -H "Content-Type: application/json" \
  -d '{
    "username": "curl_user",
    "email": "curl@example.com",
    "password": "password123",
    "role": "ROLE_PLAYER"
  }'

# Create a transaction
curl -X POST http://localhost:8084/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "type": "DEPOSIT",
    "amount": 1000.0,
    "playerId": 1
  }'
```

### VS Code REST Client

Utilisez le fichier `API_EXAMPLES.http` directement dans VS Code avec l'extension REST Client.

## 🎓 Prochaines Étapes

Maintenant que votre API fonctionne, vous pouvez :

1. **Explorer tous les endpoints** dans Swagger UI
2. **Tester les validations** avec des données invalides
3. **Créer vos propres joueurs et transactions**
4. **Intégrer avec un frontend** (React, Angular, Vue.js)
5. **Ajouter de nouvelles fonctionnalités** (voir README_PLAYER_TRANSACTION.md)

## 📚 Documentation Complète

- **README_PLAYER_TRANSACTION.md** : Documentation détaillée de l'API
- **PROJECT_STRUCTURE.md** : Architecture et structure du projet
- **API_EXAMPLES.http** : Exemples de requêtes HTTP

## ✨ Félicitations !

Vous avez maintenant un backend Spring Boot fonctionnel avec :
- ✅ API REST complète
- ✅ Swagger UI pour les tests
- ✅ Validations robustes
- ✅ Gestion d'erreurs professionnelle
- ✅ Données de test pré-chargées

**Bon développement ! 🚀**

---

**Support :** Pour toute question, consultez les logs de l'application ou la documentation Spring Boot officielle.

