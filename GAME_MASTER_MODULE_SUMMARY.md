# 🎮 MODULE GAME MASTER - RÉSUMÉ COMPLET

## ✅ Statut d'implémentation : COMPLET

Tous les composants du module Game Master ont été implémentés avec succès.

---

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Composants implémentés](#composants-implémentés)
3. [Structure de la base de données](#structure-de-la-base-de-données)
4. [Endpoints API](#endpoints-api)
5. [Sécurité](#sécurité)
6. [Guide d'utilisation](#guide-dutilisation)
7. [Tests](#tests)

---

## 🎯 Vue d'ensemble

Le module Game Master permet à un utilisateur ayant le rôle `ROLE_GAME_MASTER` de :
- ✅ Créer et gérer des sessions de jeu boursières
- ✅ Ajouter/retirer des joueurs aux sessions
- ✅ Démarrer, pauser, reprendre et clôturer des sessions
- ✅ Suivre les performances et classements des joueurs
- ✅ Consulter l'historique des sessions

---

## 🛠️ Composants implémentés

### 1. Entités (Entities)

#### ✅ `Role.java`
```java
public enum Role {
    ROLE_ADMIN,
    ROLE_PLAYER,
    ROLE_GAME_MASTER  // ← Nouveau rôle ajouté
}
```

#### ✅ `GameSession.java`
Représente une session de jeu avec :
- Informations de base (nom, description)
- Game Master (utilisateur créateur)
- Statut de la session
- Configuration financière (solde initial, devise)
- Dates de début/fin
- Liste des joueurs participants
- Configuration (nombre max de joueurs, late join autorisé)

**Champs principaux :**
- `id` : Identifiant unique
- `name` : Nom de la session
- `gameMaster` : Référence au Game Master
- `status` : Statut (CREATED, READY, ACTIVE, PAUSED, COMPLETED, CANCELLED)
- `initialBalance` : Solde de départ pour chaque joueur
- `currency` : Devise (par défaut USD)
- `startDate` / `endDate` : Période de jeu
- `sessionPlayers` : Liste des joueurs

#### ✅ `SessionPlayer.java`
Représente la participation d'un joueur dans une session :
- Références à la session et au joueur
- Soldes (initial, actuel, portfolio)
- Statistiques de performance (profit/perte, classement)
- Activité (date de join, dernière activité)

**Champs principaux :**
- `initialBalance` : Solde de départ
- `currentBalance` : Solde actuel
- `portfolioValue` : Valeur du portfolio
- `totalValue` : Valeur totale (cash + portfolio)
- `profitLoss` : Gain/Perte
- `profitLossPercentage` : % de gain/perte
- `ranking` : Classement dans la session
- `tradesCount` : Nombre de transactions

#### ✅ `SessionStatus.java`
Enum représentant les états possibles d'une session :
- `CREATED` : Session créée, en attente de joueurs
- `READY` : Tous les joueurs ajoutés, prête à démarrer
- `ACTIVE` : Session en cours
- `PAUSED` : Session en pause
- `COMPLETED` : Session terminée normalement
- `CANCELLED` : Session annulée

---

### 2. Repositories

#### ✅ `GameSessionRepository.java`
Repository avec méthodes personnalisées :
- `findByGameMasterId()` : Sessions d'un Game Master
- `findByStatus()` : Sessions par statut
- `findCurrentlyActiveSessions()` : Sessions actuellement actives
- `findUpcomingSessions()` : Sessions à venir
- `findCompletedSessions()` : Sessions terminées
- `isPlayerInSession()` : Vérifier si un joueur est dans une session
- `countActiveSessionsByGameMaster()` : Compter les sessions actives d'un GM

#### ✅ `SessionPlayerRepository.java`
Repository pour les joueurs de session :
- `findByGameSessionId()` : Tous les joueurs d'une session
- `findByGameSessionIdAndPlayerId()` : Joueur spécifique dans une session
- `findLeaderboard()` : Classement par valeur totale
- `findLeaderboardByPerformance()` : Classement par performance
- `findPlayerHistory()` : Historique des sessions d'un joueur
- `findTopPerformers()` : Top joueurs d'une session

---

### 3. DTOs (Data Transfer Objects)

#### ✅ `CreateSessionRequest.java`
DTO pour créer une session :
```java
- name (requis, 3-100 caractères)
- description (max 500 caractères)
- initialBalance (requis, > 0)
- currency (max 10 caractères)
- startDate (requis, futur)
- endDate (requis, futur)
- maxPlayers (min 2)
- allowLateJoin (boolean)
```

#### ✅ `UpdateSessionRequest.java`
DTO pour modifier une session (tous les champs optionnels)

#### ✅ `GameSessionDTO.java`
DTO de retour avec toutes les informations de la session

#### ✅ `SessionPlayerDTO.java`
DTO de retour avec les informations d'un joueur dans une session

#### ✅ `SessionLeaderboardDTO.java`
DTO pour le classement d'une session

#### ✅ `AddPlayerRequest.java`
DTO pour ajouter un joueur à une session

---

### 4. Services

#### ✅ `IGameSessionService.java` (Interface)
Contrat de service avec toutes les méthodes

#### ✅ `GameSessionService.java` (Implémentation)
Service métier complet avec :

**Gestion des sessions :**
- `createSession()` : Créer une session
- `updateSession()` : Modifier une session
- `getSessionById()` : Récupérer une session
- `getSessionsByGameMaster()` : Sessions d'un GM
- `getActiveSessions()` : Sessions actives
- `getUpcomingSessions()` : Sessions à venir
- `deleteSession()` : Supprimer une session

**Gestion des joueurs :**
- `addPlayerToSession()` : Ajouter un joueur
- `removePlayerFromSession()` : Retirer un joueur
- `getSessionPlayers()` : Liste des joueurs

**Contrôle de session :**
- `startSession()` : Démarrer
- `pauseSession()` : Mettre en pause
- `resumeSession()` : Reprendre
- `completeSession()` : Terminer
- `cancelSession()` : Annuler

**Statistiques :**
- `getSessionLeaderboard()` : Classement
- `updateSessionRankings()` : Recalculer les classements
- `updatePlayerStatistics()` : Mettre à jour les stats d'un joueur
- `getPlayerPerformance()` : Performance d'un joueur
- `getPlayerSessionHistory()` : Historique d'un joueur

**Validations incluses :**
- Vérification des droits du Game Master
- Validation des dates (fin après début)
- Vérification du statut de la session
- Contrôle de capacité (session pleine)
- Prévention des doublons (joueur déjà dans la session)

---

### 5. Contrôleur REST

#### ✅ `GameMasterController.java`
Contrôleur exposant tous les endpoints REST sous `/api/game-master`

**Endpoints implémentés :** (voir section [Endpoints API](#endpoints-api))

---

## 🗄️ Structure de la base de données

### Table : `game_sessions`
```sql
CREATE TABLE game_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    game_master_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    initial_balance DECIMAL(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    max_players INT,
    allow_late_join BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (game_master_id) REFERENCES player(id)
);
```

### Table : `session_players`
```sql
CREATE TABLE session_players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_session_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    initial_balance DECIMAL(19,2) NOT NULL,
    current_balance DECIMAL(19,2) NOT NULL,
    portfolio_value DECIMAL(19,2) NOT NULL,
    total_value DECIMAL(19,2) NOT NULL,
    profit_loss DECIMAL(19,2),
    profit_loss_percentage DOUBLE,
    ranking INT,
    trades_count INT,
    joined_at DATETIME NOT NULL,
    last_activity_at DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (game_session_id) REFERENCES game_sessions(id),
    FOREIGN KEY (player_id) REFERENCES player(id),
    UNIQUE KEY unique_session_player (game_session_id, player_id)
);
```

---

## 🔌 Endpoints API

**Base URL :** `/api/game-master`

### 📂 Gestion des sessions

| Méthode | Endpoint | Description | Rôle requis |
|---------|----------|-------------|-------------|
| POST | `/sessions` | Créer une session | GAME_MASTER |
| PUT | `/sessions/{id}` | Modifier une session | GAME_MASTER |
| GET | `/sessions/{id}` | Obtenir une session | GAME_MASTER, PLAYER, ADMIN |
| GET | `/my-sessions` | Mes sessions | GAME_MASTER |
| GET | `/sessions/active` | Sessions actives | GAME_MASTER, PLAYER, ADMIN |
| GET | `/sessions/upcoming` | Sessions à venir | GAME_MASTER, PLAYER, ADMIN |
| DELETE | `/sessions/{id}` | Supprimer une session | GAME_MASTER |

### 👥 Gestion des joueurs

| Méthode | Endpoint | Description | Rôle requis |
|---------|----------|-------------|-------------|
| POST | `/sessions/{id}/players` | Ajouter un joueur | GAME_MASTER |
| DELETE | `/sessions/{id}/players/{playerId}` | Retirer un joueur | GAME_MASTER |
| GET | `/sessions/{id}/players` | Liste des joueurs | GAME_MASTER, PLAYER, ADMIN |

### 🎮 Contrôle de session

| Méthode | Endpoint | Description | Rôle requis |
|---------|----------|-------------|-------------|
| POST | `/sessions/{id}/start` | Démarrer une session | GAME_MASTER |
| POST | `/sessions/{id}/pause` | Mettre en pause | GAME_MASTER |
| POST | `/sessions/{id}/resume` | Reprendre | GAME_MASTER |
| POST | `/sessions/{id}/complete` | Terminer | GAME_MASTER |
| POST | `/sessions/{id}/cancel` | Annuler | GAME_MASTER |

### 📊 Statistiques et Classements

| Méthode | Endpoint | Description | Rôle requis |
|---------|----------|-------------|-------------|
| GET | `/sessions/{id}/leaderboard` | Classement | GAME_MASTER, PLAYER, ADMIN |
| POST | `/sessions/{id}/update-rankings` | Recalculer les classements | GAME_MASTER |
| GET | `/sessions/{id}/players/{playerId}/performance` | Performance d'un joueur | GAME_MASTER, ADMIN |
| GET | `/players/{playerId}/history` | Historique d'un joueur | GAME_MASTER, ADMIN |

---

## 🔐 Sécurité

### Configuration Spring Security

Le fichier `SecurityConfig.java` inclut la configuration pour le rôle GAME_MASTER :

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/game-master/**")
    .hasAnyRole("GAME_MASTER", "ADMIN")
    // ...
)
```

### Contrôle d'accès par annotation

Chaque endpoint utilise `@PreAuthorize` pour vérifier le rôle :

```java
@PreAuthorize("hasRole('ROLE_GAME_MASTER')")
public ResponseEntity<?> createSession(...)
```

### Validations métier

Le service valide que seul le Game Master propriétaire d'une session peut la modifier :

```java
private void validateGameMaster(GameSession session, Long gameMasterId) {
    if (!session.getGameMaster().getId().equals(gameMasterId)) {
        throw new IllegalArgumentException("Seul le Game Master...");
    }
}
```

---

## 📖 Guide d'utilisation

### 1. Créer un utilisateur Game Master

Avant de pouvoir utiliser les fonctionnalités, un utilisateur doit avoir le rôle `ROLE_GAME_MASTER`.

**Option A : Via l'inscription et mise à jour manuelle en BDD**
```sql
UPDATE player SET role = 'ROLE_GAME_MASTER' WHERE email = 'gamemaster@example.com';
```

**Option B : Créer directement en BDD**
```sql
INSERT INTO player (username, email, password, role) 
VALUES ('GameMaster1', 'gm@example.com', '$2a$10$...', 'ROLE_GAME_MASTER');
```

### 2. S'authentifier

```http
POST http://localhost:8084/api/auth/login
Content-Type: application/json

{
  "username": "GameMaster1",
  "password": "votre_mot_de_passe"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "GameMaster1",
  "email": "gm@example.com",
  "role": "ROLE_GAME_MASTER"
}
```

### 3. Créer une session

```http
POST http://localhost:8084/api/game-master/sessions
Authorization: Bearer <votre_token>
Content-Type: application/json

{
  "name": "Session Bourse Printemps 2025",
  "description": "Compétition de trading pour débutants",
  "initialBalance": 10000.00,
  "currency": "USD",
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00",
  "maxPlayers": 10,
  "allowLateJoin": false
}
```

### 4. Ajouter des joueurs

```http
POST http://localhost:8084/api/game-master/sessions/1/players
Authorization: Bearer <votre_token>
Content-Type: application/json

{
  "playerId": 5
}
```

### 5. Démarrer la session

```http
POST http://localhost:8084/api/game-master/sessions/1/start
Authorization: Bearer <votre_token>
```

### 6. Consulter le classement

```http
GET http://localhost:8084/api/game-master/sessions/1/leaderboard
Authorization: Bearer <votre_token>
```

### 7. Terminer la session

```http
POST http://localhost:8084/api/game-master/sessions/1/complete
Authorization: Bearer <votre_token>
```

---

## 🧪 Tests

### Vérification de l'implémentation

1. **Vérifier que le serveur démarre sans erreur**
```bash
mvn spring-boot:run
```

2. **Accéder à Swagger UI**
```
http://localhost:8084/swagger-ui.html
```

3. **Vérifier les tables en base de données**
```sql
SHOW TABLES LIKE '%session%';
DESCRIBE game_sessions;
DESCRIBE session_players;
```

### Scénario de test complet

Voir le fichier `API_EXAMPLES_GAME_MASTER.http` pour un scénario de test complet.

---

## 📊 Workflow Game Master

```
1. [CREATED] Créer une session
         ↓
2. [CREATED] Ajouter des joueurs
         ↓
3. [ACTIVE] Démarrer la session
         ↓
4. [ACTIVE] Les joueurs effectuent des transactions
         ↓
5. [ACTIVE] Consulter le classement en temps réel
         ↓
6. [PAUSED] (Optionnel) Mettre en pause
         ↓
7. [ACTIVE] (Optionnel) Reprendre
         ↓
8. [COMPLETED] Terminer la session
         ↓
9. [COMPLETED] Consulter les résultats finaux
```

---

## ✨ Fonctionnalités avancées

### Statistiques automatiques
- Calcul automatique du profit/perte
- Calcul du pourcentage de gain/perte
- Classement automatique des joueurs

### Validations intelligentes
- Impossible de modifier une session démarrée
- Impossible d'ajouter des joueurs à une session active (sauf si allowLateJoin)
- Vérification de capacité maximale
- Prévention des doublons

### Gestion de l'état
- Transitions d'état validées
- Historique des temps (création, début effectif, fin effective)
- Traçabilité complète

---

## 📦 Fichiers du module

```
src/main/java/tn/esprit/piboursebackend/GameSession/
├── Controllers/
│   └── GameMasterController.java
├── DTOs/
│   ├── AddPlayerRequest.java
│   ├── CreateSessionRequest.java
│   ├── GameSessionDTO.java
│   ├── SessionLeaderboardDTO.java
│   ├── SessionPlayerDTO.java
│   └── UpdateSessionRequest.java
├── Entities/
│   ├── GameSession.java
│   ├── SessionPlayer.java
│   └── SessionStatus.java
├── Repositories/
│   ├── GameSessionRepository.java
│   └── SessionPlayerRepository.java
└── Services/
    ├── GameSessionService.java
    └── IGameSessionService.java
```

---

## 🎉 Conclusion

Le module Game Master est **100% fonctionnel** et prêt à l'emploi. 

Tous les livrables demandés ont été implémentés :
✅ Entité GameSession
✅ Extension du rôle ROLE_GAME_MASTER
✅ Services de gestion complets
✅ Contrôleur REST avec tous les endpoints
✅ Sécurité configurée
✅ Validation et gestion d'erreurs
✅ Documentation Swagger intégrée

Le système est opérationnel et peut être testé immédiatement !

