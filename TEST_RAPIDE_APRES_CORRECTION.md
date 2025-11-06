# 🧪 Test rapide après correction du problème 401

## ⏱️ Temps estimé : 5 minutes

Ce guide vous permet de vérifier rapidement que le problème d'authentification JWT est résolu.

---

## 📋 Prérequis

- ✅ Application démarrée (`mvn spring-boot:run`)
- ✅ Base de données MySQL en cours d'exécution
- ✅ Utilisateur ADMIN ou GAME_MASTER créé

---

## 🚀 OPTION 1 : Test via Swagger UI (Recommandé)

### Étape 1 : Créer un utilisateur ADMIN

```bash
# Se connecter à MySQL
mysql -u root -p pibourse

# Exécuter le script
source create_admin_user.sql

# Ou copier-coller ces commandes SQL :
```

```sql
INSERT INTO players (username, email, password, role) 
VALUES ('Admin', 'admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6', 'ROLE_ADMIN');

SET @admin_id = LAST_INSERT_ID();

INSERT INTO wallets (player_id, balance, currency, created_at, updated_at)
VALUES (@admin_id, 100000.00, 'USD', NOW(), NOW());
```

### Étape 2 : Ouvrir Swagger UI

Ouvrez votre navigateur : **http://localhost:8084/swagger-ui.html**

### Étape 3 : Se connecter

1. Trouvez l'endpoint `POST /api/auth/login`
2. Cliquez sur **"Try it out"**
3. Entrez :

```json
{
  "username": "admin@test.com",
  "password": "Password123!"
}
```

4. Cliquez sur **"Execute"**

**✅ Réponse attendue : 200 OK**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "Admin",
  "email": "admin@test.com",
  "role": "ROLE_ADMIN"
}
```

5. **Copiez le token** (la longue chaîne de caractères)

### Étape 4 : Autoriser dans Swagger

1. Cliquez sur le bouton **"Authorize"** 🔒 en haut à droite
2. Dans le champ qui s'ouvre, entrez :
   ```
   Bearer VOTRE_TOKEN_COPIE_ICI
   ```
   ⚠️ **Attention** : N'oubliez pas le mot "Bearer " avec un espace après !

3. Cliquez sur **"Authorize"**
4. Vous devriez voir **"Authorized"** avec un ✅
5. Fermez la fenêtre

### Étape 5 : Créer une session de jeu

1. Trouvez l'endpoint `POST /api/game-master/sessions`
2. Cliquez sur **"Try it out"**
3. Entrez :

```json
{
  "name": "Session Test Correction",
  "description": "Test après correction du bug 401",
  "initialBalance": 10000.0,
  "currency": "USD",
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00",
  "maxPlayers": 10,
  "allowLateJoin": true
}
```

4. Cliquez sur **"Execute"**

**✅ Réponse attendue : 201 Created**

```json
{
  "id": 1,
  "name": "Session Test Correction",
  "description": "Test après correction du bug 401",
  "status": "CREATED",
  "initialBalance": 10000.0,
  "currency": "USD",
  ...
}
```

### ✅ Résultat

Si vous avez reçu **201 Created**, le problème est **RÉSOLU** ! 🎉

---

## 🚀 OPTION 2 : Test via curl (Terminal)

### Étape 1 : Se connecter

```bash
curl -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "admin@test.com",
  "password": "Password123!"
}'
```

**Sortie attendue :**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImlhdCI6MTY5OTIzODk5OSwiZXhwIjoxNjk5MzI1Mzk5fQ.xxxx",
  "type": "Bearer",
  "id": 1,
  "username": "Admin",
  "email": "admin@test.com",
  "role": "ROLE_ADMIN"
}
```

**Copiez le token** (sans les guillemets)

### Étape 2 : Créer une session

⚠️ **Remplacez `VOTRE_TOKEN_ICI` par le token copié à l'étape 1**

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Session Test CLI",
  "description": "Test via curl",
  "initialBalance": 10000.0,
  "currency": "USD",
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00",
  "maxPlayers": 10,
  "allowLateJoin": true
}'
```

**✅ Sortie attendue : Code 201**

```json
{
  "id": 1,
  "name": "Session Test CLI",
  "status": "CREATED",
  ...
}
```

---

## 🔍 Tests additionnels

### Test 1 : Récupérer les sessions actives

```bash
curl -X 'GET' \
  'http://localhost:8084/api/game-master/sessions/active' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI'
```

**✅ Attendu : 200 OK** avec liste (peut être vide)

### Test 2 : Récupérer mes sessions

```bash
curl -X 'GET' \
  'http://localhost:8084/api/game-master/my-sessions' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI'
```

**✅ Attendu : 200 OK** avec liste de vos sessions

### Test 3 : Ajouter un joueur (après avoir créé une session)

D'abord, créez des joueurs avec le script SQL, puis :

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions/1/players' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI' \
  -H 'Content-Type: application/json' \
  -d '{
  "playerId": 2
}'
```

**✅ Attendu : 201 Created**

---

## ❌ Que faire si ça ne fonctionne pas ?

### Erreur : 401 Unauthorized

**Causes possibles :**
1. Token expiré → Reconnectez-vous
2. Token mal copié → Vérifiez qu'il n'y a pas d'espaces
3. Préfixe "Bearer" manquant
4. Rôle incorrect

**Solution :**

```sql
-- Vérifier le rôle
SELECT id, username, email, role FROM players WHERE email = 'admin@test.com';

-- Si le rôle n'est pas ROLE_ADMIN, le corriger
UPDATE players SET role = 'ROLE_ADMIN' WHERE email = 'admin@test.com';
```

### Erreur : 403 Forbidden

**Cause :** Le rôle existe mais n'a pas les permissions

**Solution :**
1. Vérifiez que vous avez bien redémarré l'application après les modifications
2. Vérifiez que le fichier `GameMasterController.java` contient bien :
   ```java
   @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
   ```

### Erreur : Connection refused

**Cause :** L'application n'est pas démarrée

**Solution :**

```bash
mvn spring-boot:run
```

---

## ✅ Checklist de validation

Après avoir suivi ce guide, vous devriez avoir :

- [ ] ✅ Utilisateur ADMIN créé
- [ ] ✅ Connexion réussie (token obtenu)
- [ ] ✅ Token autorisé dans Swagger
- [ ] ✅ Session créée avec succès (201 Created)
- [ ] ✅ Aucune erreur 401 ou 403

---

## 🎉 Succès !

Si tous les tests sont **verts** ✅, le problème est **complètement résolu** !

Vous pouvez maintenant :
- ✅ Créer des sessions de jeu
- ✅ Ajouter des joueurs
- ✅ Gérer vos sessions
- ✅ Utiliser tous les endpoints protégés

---

## 📚 Ressources utiles

- **Guide complet** : `TROUBLESHOOTING_JWT_401.md`
- **Documentation API** : `API_EXAMPLES_GAME_MASTER.http`
- **Guide démarrage** : `QUICK_START_GAME_MASTER.md`
- **Swagger UI** : http://localhost:8084/swagger-ui.html

---

**Date :** 5 novembre 2025  
**Problème résolu :** Erreur 401 Unauthorized  
**Correction :** Annotations @PreAuthorize mises à jour


