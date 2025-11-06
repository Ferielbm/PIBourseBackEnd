# 🚀 GUIDE DE DÉMARRAGE RAPIDE - MODULE GAME MASTER

## 📋 Table des matières
1. [Prérequis](#prérequis)
2. [Configuration initiale](#configuration-initiale)
3. [Créer un utilisateur Game Master](#créer-un-utilisateur-game-master)
4. [Démarrer l'application](#démarrer-lapplication)
5. [Tests rapides](#tests-rapides)
6. [Scénario complet](#scénario-complet)
7. [Tests unitaires](#tests-unitaires)
8. [Dépannage](#dépannage)

---

## ✅ Prérequis

Avant de commencer, assurez-vous d'avoir :

- ✅ Java 17 ou supérieur
- ✅ Maven 3.6+
- ✅ MySQL 8.0+ (ou MariaDB)
- ✅ Un IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)
- ✅ Postman, Insomnia ou REST Client (pour tester les API)

---

## ⚙️ Configuration initiale

### 1. Configuration de la base de données

Créez une base de données MySQL :

```sql
CREATE DATABASE pibourse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configuration de l'application

Le fichier `application.properties` est déjà configuré. Vérifiez les paramètres suivants :

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/pibourse?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=update

# Server
server.port=8084
```

### 3. Installer les dépendances

```bash
mvn clean install
```

---

## 👤 Créer un utilisateur Game Master

### Option 1 : Via script SQL (Recommandé)

```sql
-- 1. Créer un utilisateur Game Master
INSERT INTO players (username, email, password, role) 
VALUES (
    'GameMaster1',
    'gamemaster@example.com',
    '$2a$10$YourBcryptHashedPasswordHere',  -- Hash BCrypt de "Password123!"
    'ROLE_GAME_MASTER'
);

-- 2. Créer quelques joueurs pour les tests
INSERT INTO players (username, email, password, role) 
VALUES 
    ('Player1', 'player1@example.com', '$2a$10$YourBcryptHashedPasswordHere', 'ROLE_PLAYER'),
    ('Player2', 'player2@example.com', '$2a$10$YourBcryptHashedPasswordHere', 'ROLE_PLAYER'),
    ('Player3', 'player3@example.com', '$2a$10$YourBcryptHashedPasswordHere', 'ROLE_PLAYER');
```

### Option 2 : Via l'API d'inscription

1. **Créer un compte normal :**

```http
POST http://localhost:8084/api/auth/signup
Content-Type: application/json

{
  "username": "GameMaster1",
  "email": "gamemaster@example.com",
  "password": "Password123!"
}
```

2. **Mettre à jour le rôle en base de données :**

```sql
UPDATE players SET role = 'ROLE_GAME_MASTER' WHERE email = 'gamemaster@example.com';
```

### Option 3 : Utiliser un script de données de test

Créez un fichier `data-test.sql` dans `src/main/resources/` :

```sql
-- Game Master
INSERT INTO players (id, username, email, password, role) 
VALUES (1, 'GameMaster1', 'gm@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6', 'ROLE_GAME_MASTER');

-- Players
INSERT INTO players (id, username, email, password, role) 
VALUES 
(2, 'Player1', 'p1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6', 'ROLE_PLAYER'),
(3, 'Player2', 'p2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6', 'ROLE_PLAYER'),
(4, 'Player3', 'p3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6', 'ROLE_PLAYER');

-- Wallets for all players
INSERT INTO wallets (id, player_id, balance, currency, created_at, updated_at)
VALUES 
(1, 1, 100000.00, 'USD', NOW(), NOW()),
(2, 2, 10000.00, 'USD', NOW(), NOW()),
(3, 3, 10000.00, 'USD', NOW(), NOW()),
(4, 4, 10000.00, 'USD', NOW(), NOW());
```

**Mot de passe pour tous les comptes de test : `Password123!`**

---

## 🚀 Démarrer l'application

### Méthode 1 : Via Maven

```bash
mvn spring-boot:run
```

### Méthode 2 : Via IDE

Exécutez la classe principale : `PiBourseBackEndApplication.java`

### Méthode 3 : Via JAR packagé

```bash
mvn clean package
java -jar target/PiBourseBackEnd-0.0.1-SNAPSHOT.jar
```

### ✅ Vérification du démarrage

L'application est prête quand vous voyez :

```
Started PiBourseBackEndApplication in X.XXX seconds
```

Accédez à Swagger UI : **http://localhost:8084/swagger-ui.html**

---

## 🧪 Tests rapides

### 1. Vérifier que les tables sont créées

```sql
SHOW TABLES;
-- Devrait afficher : game_sessions, session_players, players, wallets, etc.

DESCRIBE game_sessions;
DESCRIBE session_players;
```

### 2. Test de connexion

```http
POST http://localhost:8084/api/auth/login
Content-Type: application/json

{
  "username": "gm@test.com",
  "password": "Password123!"
}
```

**Réponse attendue :**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "GameMaster1",
  "email": "gm@test.com",
  "role": "ROLE_GAME_MASTER"
}
```

✅ **Copiez le token** pour les prochaines requêtes !

### 3. Créer votre première session

```http
POST http://localhost:8084/api/game-master/sessions
Authorization: Bearer VOTRE_TOKEN_ICI
Content-Type: application/json

{
  "name": "Ma Première Session",
  "description": "Session de test",
  "initialBalance": 10000.00,
  "currency": "USD",
  "startDate": "2025-11-15T09:00:00",
  "endDate": "2025-11-22T18:00:00",
  "maxPlayers": 10,
  "allowLateJoin": false
}
```

**Réponse attendue : 201 Created**

```json
{
  "id": 1,
  "name": "Ma Première Session",
  "status": "CREATED",
  "initialBalance": 10000.00,
  "playerCount": 0,
  ...
}
```

✅ **Notez l'ID de la session** (par exemple : 1)

### 4. Ajouter un joueur

```http
POST http://localhost:8084/api/game-master/sessions/1/players
Authorization: Bearer VOTRE_TOKEN_ICI
Content-Type: application/json

{
  "playerId": 2
}
```

**Réponse attendue : 201 Created**

### 5. Démarrer la session

```http
POST http://localhost:8084/api/game-master/sessions/1/start
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Réponse attendue : 200 OK** avec `"status": "ACTIVE"`

### 6. Consulter le classement

```http
GET http://localhost:8084/api/game-master/sessions/1/leaderboard
Authorization: Bearer VOTRE_TOKEN_ICI
```

---

## 🎯 Scénario complet (5 minutes)

Suivez ce scénario pour tester toutes les fonctionnalités :

### Étape 1 : Connexion
```bash
POST /api/auth/login
```

### Étape 2 : Créer une session
```bash
POST /api/game-master/sessions
```

### Étape 3 : Ajouter 3 joueurs
```bash
POST /api/game-master/sessions/1/players (playerId: 2)
POST /api/game-master/sessions/1/players (playerId: 3)
POST /api/game-master/sessions/1/players (playerId: 4)
```

### Étape 4 : Vérifier la liste
```bash
GET /api/game-master/sessions/1/players
```

### Étape 5 : Démarrer
```bash
POST /api/game-master/sessions/1/start
```

### Étape 6 : Mettre en pause
```bash
POST /api/game-master/sessions/1/pause
```

### Étape 7 : Reprendre
```bash
POST /api/game-master/sessions/1/resume
```

### Étape 8 : Consulter le classement
```bash
GET /api/game-master/sessions/1/leaderboard
```

### Étape 9 : Terminer la session
```bash
POST /api/game-master/sessions/1/complete
```

### Étape 10 : Consulter le classement final
```bash
GET /api/game-master/sessions/1/leaderboard
```

✅ **Si toutes ces étapes fonctionnent, le module est opérationnel !**

---

## 🧪 Tests unitaires

### Lancer tous les tests

```bash
mvn test
```

### Lancer uniquement les tests Game Master

```bash
mvn test -Dtest=GameSessionServiceTest
mvn test -Dtest=GameMasterControllerTest
```

### Vérifier la couverture

```bash
mvn clean test jacoco:report
# Rapport disponible dans : target/site/jacoco/index.html
```

---

## 🐛 Dépannage

### Problème 1 : Erreur 401 Unauthorized

**Cause :** Token expiré ou invalide

**Solution :**
- Reconnectez-vous pour obtenir un nouveau token
- Vérifiez que le token est bien dans le header `Authorization: Bearer TOKEN`

### Problème 2 : Erreur 403 Forbidden

**Cause :** Rôle insuffisant

**Solution :**
```sql
-- Vérifier le rôle
SELECT id, username, email, role FROM players WHERE email = 'votre@email.com';

-- Mettre à jour si nécessaire
UPDATE players SET role = 'ROLE_GAME_MASTER' WHERE email = 'votre@email.com';
```

### Problème 3 : Tables non créées

**Cause :** `spring.jpa.hibernate.ddl-auto` mal configuré

**Solution :**
```properties
spring.jpa.hibernate.ddl-auto=update
```

Redémarrez l'application.

### Problème 4 : Erreur de validation

**Message :** "Le nom doit contenir entre 3 et 100 caractères"

**Solution :** Vérifiez que vos données respectent les contraintes :
- Nom : 3-100 caractères
- Solde initial > 0
- Date de fin après date de début
- MaxPlayers ≥ 2

### Problème 5 : "Impossible d'ajouter des joueurs"

**Cause :** La session est déjà démarrée et `allowLateJoin=false`

**Solution :** 
- Ajoutez les joueurs avant de démarrer
- Ou créez une session avec `allowLateJoin=true`

### Problème 6 : Erreur de connexion MySQL

**Message :** "Access denied for user..."

**Solution :**
```properties
# Vérifiez dans application.properties
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

---

## 📚 Ressources supplémentaires

- **Documentation complète :** [GAME_MASTER_MODULE_SUMMARY.md](GAME_MASTER_MODULE_SUMMARY.md)
- **Tests API :** [API_EXAMPLES_GAME_MASTER.http](API_EXAMPLES_GAME_MASTER.http)
- **Swagger UI :** http://localhost:8084/swagger-ui.html
- **API Docs :** http://localhost:8084/v3/api-docs

---

## ✅ Checklist de validation

Avant de déployer en production, vérifiez que :

- [ ] ✅ L'application démarre sans erreur
- [ ] ✅ Les tables sont créées dans la base de données
- [ ] ✅ Connexion avec Game Master fonctionne
- [ ] ✅ Création de session réussie
- [ ] ✅ Ajout de joueurs fonctionnel
- [ ] ✅ Démarrage/Pause/Reprise/Clôture OK
- [ ] ✅ Classement mis à jour correctement
- [ ] ✅ Sécurité validée (401/403 pour accès non autorisé)
- [ ] ✅ Validations des données fonctionnelles
- [ ] ✅ Tests unitaires passent (mvn test)
- [ ] ✅ Swagger accessible et fonctionnel

---

## 🎉 Vous êtes prêt !

Le module Game Master est maintenant opérationnel. Vous pouvez :

1. **Tester les fonctionnalités** via Swagger UI ou Postman
2. **Lancer les tests unitaires** avec `mvn test`
3. **Utiliser le fichier HTTP** pour des tests rapides
4. **Intégrer avec le frontend** en utilisant les endpoints documentés

---

## 📞 Support

En cas de problème :

1. Consultez la section [Dépannage](#dépannage)
2. Vérifiez les logs de l'application
3. Consultez la documentation Swagger
4. Examinez les tests unitaires pour voir des exemples d'utilisation

---

**Bon développement ! 🚀**


