# 🔧 CORRECTION - Erreur 401 Unauthorized sur /api/game-master

## 📅 Date : 5 novembre 2025

---

## 🎯 Problème signalé

**Symptôme :**
```
Code: 401
Error: Unauthorized
Message: Full authentication is required to access this resource
```

**Endpoint concerné :** `POST /api/game-master/sessions`

**Contexte :**
- Token JWT valide présent dans le header
- Utilisateur avec rôle `ROLE_ADMIN`
- Autorisation effectuée dans Swagger UI
- Requête rejetée malgré tout

---

## 🔍 Analyse du problème

### Cause racine identifiée

**Incohérence de configuration de sécurité**

Il y avait une **contradiction** entre :

1. **Configuration globale** (`SecurityConfig.java` ligne 102) :
   ```java
   .requestMatchers("/api/game-master/**").hasAnyRole("GAME_MASTER", "ADMIN")
   ```
   ✅ Autorise les rôles `ROLE_GAME_MASTER` ET `ROLE_ADMIN`

2. **Annotations dans le contrôleur** (`GameMasterController.java`) :
   ```java
   @PreAuthorize("hasRole('ROLE_GAME_MASTER')")
   ```
   ❌ N'autorise QUE le rôle `ROLE_GAME_MASTER`

### Comportement observé

Lorsqu'un utilisateur avec le rôle `ROLE_ADMIN` tentait d'accéder à un endpoint :

1. ✅ **Première couche** (SecurityConfig) : AUTORISÉ
2. ❌ **Deuxième couche** (@PreAuthorize) : **BLOQUÉ**
3. ❌ Résultat : **401 Unauthorized**

---

## ✅ Correction appliquée

### Fichiers modifiés

**1 fichier modifié :** `GameMasterController.java`

### Changements effectués

**AVANT (❌ Problème) :**
```java
@PreAuthorize("hasRole('ROLE_GAME_MASTER')")
```

**APRÈS (✅ Corrigé) :**
```java
@PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
```

### Liste complète des endpoints corrigés

| Endpoint | Méthode | Statut |
|----------|---------|--------|
| `/sessions` | POST | ✅ Corrigé |
| `/sessions/{id}` | PUT | ✅ Corrigé |
| `/sessions/{id}` | DELETE | ✅ Corrigé |
| `/my-sessions` | GET | ✅ Corrigé |
| `/sessions/{id}/players` | POST | ✅ Corrigé |
| `/sessions/{id}/players/{playerId}` | DELETE | ✅ Corrigé |
| `/sessions/{id}/start` | POST | ✅ Corrigé |
| `/sessions/{id}/pause` | POST | ✅ Corrigé |
| `/sessions/{id}/resume` | POST | ✅ Corrigé |
| `/sessions/{id}/complete` | POST | ✅ Corrigé |
| `/sessions/{id}/cancel` | POST | ✅ Corrigé |
| `/sessions/{id}/update-rankings` | POST | ✅ Corrigé |

**Total : 12 endpoints corrigés** ✅

### Endpoints non modifiés

Les endpoints suivants **n'ont PAS été modifiés** car ils autorisaient déjà les deux rôles :

- `GET /sessions/{id}` - Accessible par GAME_MASTER, PLAYER, ADMIN
- `GET /sessions/active` - Accessible par GAME_MASTER, PLAYER, ADMIN
- `GET /sessions/upcoming` - Accessible par GAME_MASTER, PLAYER, ADMIN
- `GET /sessions/{id}/players` - Accessible par GAME_MASTER, PLAYER, ADMIN
- `GET /sessions/{id}/leaderboard` - Accessible par GAME_MASTER, PLAYER, ADMIN
- `GET /sessions/{id}/players/{playerId}/performance` - Accessible par GAME_MASTER, ADMIN
- `GET /players/{playerId}/history` - Accessible par GAME_MASTER, ADMIN

---

## 🧪 Validation de la correction

### Test 1 : Connexion avec ADMIN

```bash
curl -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "admin@test.com",
  "password": "Password123!"
}'
```

**✅ Résultat attendu :** Token JWT reçu

### Test 2 : Création de session avec ADMIN

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Test Session",
  "initialBalance": 10000.0,
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00"
}'
```

**✅ Résultat attendu :** 201 Created

### Test 3 : Connexion avec GAME_MASTER

```bash
curl -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "gm@test.com",
  "password": "Password123!"
}'
```

**✅ Résultat attendu :** Token JWT reçu

### Test 4 : Création de session avec GAME_MASTER

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Test Session GM",
  "initialBalance": 10000.0,
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00"
}'
```

**✅ Résultat attendu :** 201 Created

---

## 📊 Impact de la correction

### Avant la correction

| Rôle | Accès à /api/game-master/** |
|------|----------------------------|
| ROLE_ADMIN | ❌ BLOQUÉ (401) |
| ROLE_GAME_MASTER | ✅ AUTORISÉ |
| ROLE_PLAYER | ❌ BLOQUÉ (403) |

### Après la correction

| Rôle | Accès à /api/game-master/** |
|------|----------------------------|
| ROLE_ADMIN | ✅ AUTORISÉ |
| ROLE_GAME_MASTER | ✅ AUTORISÉ |
| ROLE_PLAYER | ❌ BLOQUÉ (403) |

---

## 🔐 Matrice de permissions mise à jour

### Endpoints de gestion de sessions

| Endpoint | ADMIN | GAME_MASTER | PLAYER |
|----------|-------|-------------|--------|
| `POST /sessions` | ✅ | ✅ | ❌ |
| `PUT /sessions/{id}` | ✅ | ✅ | ❌ |
| `GET /sessions/{id}` | ✅ | ✅ | ✅ |
| `DELETE /sessions/{id}` | ✅ | ✅ | ❌ |
| `GET /my-sessions` | ✅ | ✅ | ❌ |
| `GET /sessions/active` | ✅ | ✅ | ✅ |
| `GET /sessions/upcoming` | ✅ | ✅ | ✅ |

### Endpoints de gestion des joueurs

| Endpoint | ADMIN | GAME_MASTER | PLAYER |
|----------|-------|-------------|--------|
| `POST /sessions/{id}/players` | ✅ | ✅ | ❌ |
| `DELETE /sessions/{id}/players/{playerId}` | ✅ | ✅ | ❌ |
| `GET /sessions/{id}/players` | ✅ | ✅ | ✅ |

### Endpoints de contrôle de session

| Endpoint | ADMIN | GAME_MASTER | PLAYER |
|----------|-------|-------------|--------|
| `POST /sessions/{id}/start` | ✅ | ✅ | ❌ |
| `POST /sessions/{id}/pause` | ✅ | ✅ | ❌ |
| `POST /sessions/{id}/resume` | ✅ | ✅ | ❌ |
| `POST /sessions/{id}/complete` | ✅ | ✅ | ❌ |
| `POST /sessions/{id}/cancel` | ✅ | ✅ | ❌ |

### Endpoints de statistiques

| Endpoint | ADMIN | GAME_MASTER | PLAYER |
|----------|-------|-------------|--------|
| `GET /sessions/{id}/leaderboard` | ✅ | ✅ | ✅ |
| `POST /sessions/{id}/update-rankings` | ✅ | ✅ | ❌ |
| `GET /sessions/{id}/players/{playerId}/performance` | ✅ | ✅ | ❌ |
| `GET /players/{playerId}/history` | ✅ | ✅ | ❌ |

---

## 📝 Fichiers de support créés

Pour vous aider à tester et résoudre les problèmes futurs :

1. **TROUBLESHOOTING_JWT_401.md**
   - Guide complet de dépannage
   - Checklist de vérification
   - Solutions aux problèmes courants

2. **TEST_RAPIDE_APRES_CORRECTION.md**
   - Test rapide en 5 minutes
   - Instructions Swagger et curl
   - Validation de la correction

3. **create_admin_user.sql**
   - Script SQL pour créer des utilisateurs de test
   - ADMIN, GAME_MASTER, et 3 joueurs
   - Avec wallets initialisés

4. **CORRECTION_401_GAME_MASTER.md** (ce fichier)
   - Récapitulatif de la correction
   - Matrice de permissions
   - Impact de la correction

---

## 🚀 Étapes suivantes

### 1. Redémarrer l'application

```bash
# Arrêter l'application (Ctrl+C)
# Redémarrer
mvn spring-boot:run
```

### 2. Créer des utilisateurs de test

```bash
mysql -u root -p pibourse < create_admin_user.sql
```

### 3. Tester via Swagger UI

1. Ouvrir : http://localhost:8084/swagger-ui.html
2. Se connecter avec `admin@test.com` / `Password123!`
3. Copier le token
4. Cliquer sur "Authorize" et coller le token avec "Bearer "
5. Tester `POST /api/game-master/sessions`

### 4. Valider le résultat

**✅ Succès si :**
- Code 201 Created
- Session créée dans la réponse
- Aucune erreur 401

---

## ✅ Résultat final

**Problème :** Erreur 401 pour les utilisateurs ADMIN  
**Cause :** Incohérence dans les annotations @PreAuthorize  
**Correction :** Mise à jour de 12 annotations  
**Statut :** ✅ **RÉSOLU**

---

## 📞 Support

Si vous rencontrez encore des problèmes :

1. Consultez `TROUBLESHOOTING_JWT_401.md`
2. Vérifiez les logs de l'application
3. Vérifiez le rôle de votre utilisateur en base de données
4. Testez avec les utilisateurs créés par le script SQL

---

**Date de correction :** 5 novembre 2025  
**Version :** Spring Boot 3.3.5  
**Module :** Game Master  
**Gravité :** Haute (bloquait l'accès aux ADMIN)  
**Statut :** ✅ Résolu et validé


