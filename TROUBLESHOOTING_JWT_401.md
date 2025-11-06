# 🔧 Guide de dépannage - Erreur 401 Unauthorized

## 📋 Problème résolu

**Erreur** : 401 Unauthorized lors de l'utilisation de l'API `/api/game-master/sessions`

**Cause identifiée** : Incohérence entre la configuration globale de sécurité et les annotations `@PreAuthorize` dans le contrôleur.

---

## ✅ Correction appliquée

### Avant (❌ Problème)

**Dans `GameMasterController.java` :**
```java
@PreAuthorize("hasRole('ROLE_GAME_MASTER')")  // ❌ Bloquait les ADMIN
```

### Après (✅ Corrigé)

**Dans `GameMasterController.java` :**
```java
@PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")  // ✅ Autorise ADMIN et GAME_MASTER
```

**Tous les endpoints suivants ont été corrigés :**
- ✅ `POST /sessions` - Créer une session
- ✅ `PUT /sessions/{id}` - Modifier une session
- ✅ `DELETE /sessions/{id}` - Supprimer une session
- ✅ `GET /my-sessions` - Mes sessions
- ✅ `POST /sessions/{id}/players` - Ajouter un joueur
- ✅ `DELETE /sessions/{id}/players/{playerId}` - Retirer un joueur
- ✅ `POST /sessions/{id}/start` - Démarrer
- ✅ `POST /sessions/{id}/pause` - Pause
- ✅ `POST /sessions/{id}/resume` - Reprendre
- ✅ `POST /sessions/{id}/complete` - Terminer
- ✅ `POST /sessions/{id}/cancel` - Annuler
- ✅ `POST /sessions/{id}/update-rankings` - Recalculer classements

---

## 🚀 Étapes pour tester la correction

### 1️⃣ Redémarrer l'application

```bash
# Arrêter l'application (Ctrl+C)
# Puis redémarrer
mvn spring-boot:run
```

### 2️⃣ Se connecter et obtenir un token

**Via Swagger UI :** http://localhost:8084/swagger-ui.html

1. Allez sur l'endpoint `POST /api/auth/login`
2. Cliquez sur "Try it out"
3. Entrez vos credentials :

```json
{
  "username": "votre@email.com",
  "password": "VotreMotDePasse"
}
```

4. Récupérez le token dans la réponse

**Via curl :**

```bash
curl -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "admin@example.com",
  "password": "Password123!"
}'
```

**Réponse attendue :**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTY5OTIzNDU2NywiZXhwIjoxNjk5MzIwOTY3fQ...",
  "type": "Bearer",
  "id": 1,
  "username": "Admin",
  "email": "admin@example.com",
  "role": "ROLE_ADMIN"
}
```

### 3️⃣ Autoriser dans Swagger

1. Cliquez sur le bouton **"Authorize"** (🔒) en haut de Swagger UI
2. Dans le champ "Value", entrez : `Bearer VOTRE_TOKEN_ICI`
   - ⚠️ **N'oubliez pas le préfixe "Bearer "** (avec un espace après)
3. Cliquez sur "Authorize"
4. Vous devriez voir "Authorized" avec un ✅

### 4️⃣ Tester la création de session

**Via Swagger UI :**

1. Allez sur `POST /api/game-master/sessions`
2. Cliquez sur "Try it out"
3. Entrez les données :

```json
{
  "name": "Session Test 2025",
  "description": "Test après correction",
  "initialBalance": 10000.0,
  "currency": "USD",
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00",
  "maxPlayers": 10,
  "allowLateJoin": true
}
```

4. Cliquez sur "Execute"

**Réponse attendue : 201 Created** ✅

**Via curl :**

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Session Test 2025",
  "description": "Test après correction",
  "initialBalance": 10000.0,
  "currency": "USD",
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00",
  "maxPlayers": 10,
  "allowLateJoin": true
}'
```

---

## 🔍 Si le problème persiste

### Vérification 1 : Le rôle de l'utilisateur

```sql
-- Vérifier le rôle de votre utilisateur
SELECT id, username, email, role FROM players WHERE email = 'votre@email.com';
```

**Résultat attendu :**
- `role` doit être soit `ROLE_ADMIN` soit `ROLE_GAME_MASTER`

**Si ce n'est pas le cas, mettre à jour :**

```sql
UPDATE players SET role = 'ROLE_ADMIN' WHERE email = 'votre@email.com';
-- OU
UPDATE players SET role = 'ROLE_GAME_MASTER' WHERE email = 'votre@email.com';
```

### Vérification 2 : Le token est-il valide ?

Le token JWT expire après 24 heures par défaut (`jwt.expirationMs=86400000` dans `application.properties`).

**Solutions :**
1. Reconnectez-vous pour obtenir un nouveau token
2. Vérifiez que le token n'est pas tronqué lors du copier-coller

### Vérification 3 : Format du header Authorization

Le header doit être **exactement** :

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Erreurs courantes :**
- ❌ `Bearer eyJhbGciOiJIUzUxMiJ9...` (pas d'espace après Bearer)
- ❌ `eyJhbGciOiJIUzUxMiJ9...` (manque "Bearer")
- ❌ `Bearer: eyJhbGciOiJIUzUxMiJ9...` (deux-points après Bearer)

### Vérification 4 : Configuration de sécurité

Vérifiez que dans `SecurityConfig.java`, la ligne suivante est présente :

```java
.requestMatchers("/api/game-master/**").hasAnyRole("GAME_MASTER", "ADMIN")
```

### Vérification 5 : Le filtre JWT est-il actif ?

Vérifiez les logs de l'application. Vous devriez voir :

```
INFO  - JWT filter: processing request for /api/game-master/sessions
```

Si vous ne voyez pas ce log, le filtre JWT n'est pas appliqué.

### Vérification 6 : CORS

Si vous testez depuis un frontend, vérifiez que CORS est bien configuré dans le contrôleur :

```java
@CrossOrigin(origins = "*", maxAge = 3600)
```

---

## 🧪 Tests de diagnostic

### Test 1 : Endpoint public

Testez un endpoint public pour vérifier que l'application fonctionne :

```bash
curl -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "test@example.com",
  "password": "password"
}'
```

**Si cela ne fonctionne pas** → Problème avec l'application elle-même.

### Test 2 : Endpoint protégé sans token

```bash
curl -X 'GET' 'http://localhost:8084/api/game-master/sessions/active'
```

**Réponse attendue : 401 Unauthorized** ✅

### Test 3 : Endpoint protégé avec token

```bash
curl -X 'GET' \
  'http://localhost:8084/api/game-master/sessions/active' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI'
```

**Réponse attendue : 200 OK** avec liste de sessions ✅

---

## 📊 Checklist de validation

- [ ] ✅ Application redémarrée après les modifications
- [ ] ✅ Connexion réussie avec `POST /api/auth/login`
- [ ] ✅ Token récupéré dans la réponse
- [ ] ✅ Token copié AVEC le préfixe "Bearer "
- [ ] ✅ Autorisation effectuée dans Swagger UI
- [ ] ✅ Rôle de l'utilisateur vérifié (ADMIN ou GAME_MASTER)
- [ ] ✅ Token non expiré (< 24h)
- [ ] ✅ Pas d'espaces ou caractères parasites dans le token
- [ ] ✅ L'endpoint testé renvoie 201 Created ou 200 OK

---

## 🐛 Problèmes spécifiques et solutions

### Problème : "Token has expired"

**Solution :**
```bash
# Reconnectez-vous pour obtenir un nouveau token
curl -X 'POST' 'http://localhost:8084/api/auth/login' ...
```

### Problème : "Invalid JWT signature"

**Cause :** Le secret JWT a changé ou le token a été corrompu.

**Solution :**
1. Vérifiez `jwt.secret` dans `application.properties`
2. Reconnectez-vous pour obtenir un nouveau token

### Problème : "Access is denied"

**Cause :** Le rôle de l'utilisateur n'est pas suffisant.

**Solution :**
```sql
UPDATE players SET role = 'ROLE_ADMIN' WHERE email = 'votre@email.com';
```

### Problème : Swagger n'envoie pas le token

**Solution :**
1. Déconnectez-vous de Swagger (bouton "Logout")
2. Reconnectez-vous avec un nouveau token
3. Assurez-vous de voir "Authorized" ✅

---

## 📝 Commandes utiles

### Redémarrer l'application
```bash
mvn spring-boot:run
```

### Vérifier les logs
```bash
tail -f app-logs.txt
```

### Tester la base de données
```bash
mysql -u root -p pibourse
```

### Compiler après modifications
```bash
mvn clean install
```

---

## 🎯 Résultat attendu après correction

Après avoir appliqué les corrections et suivi ce guide :

✅ **Connexion réussie** - Token obtenu  
✅ **Autorisation Swagger** - "Authorized" affiché  
✅ **Création de session** - Code 201 Created  
✅ **Tous les endpoints fonctionnels** - Code 200/201  

---

## 📞 Support additionnel

Si le problème persiste après avoir suivi ce guide :

1. **Vérifiez les logs** de l'application pour des erreurs spécifiques
2. **Testez avec curl** pour éliminer les problèmes liés à Swagger
3. **Vérifiez la base de données** pour confirmer le rôle de l'utilisateur
4. **Redémarrez MySQL** si nécessaire

---

**Date de création :** 5 novembre 2025  
**Problème résolu :** Incohérence dans les annotations @PreAuthorize  
**Version Spring Boot :** 3.3.5


