# 🚀 Commandes exactes pour tester le module Game Master

## ⚠️ IMPORTANT

Vous avez reçu une erreur **401 Unauthorized** parce que vous n'avez **PAS envoyé de token JWT** dans votre requête.

Un endpoint protégé nécessite **TOUJOURS** un header `Authorization: Bearer TOKEN`.

---

## ✅ Solution en 2 étapes

### ÉTAPE 1 : Se connecter et obtenir le token

**Commande curl :**

```bash
curl -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin@test.com",
    "password": "Password123!"
  }'
```

**Réponse attendue :**

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

**➡️ COPIEZ LE TOKEN** (la longue chaîne après `"token":`)

---

### ÉTAPE 2 : Créer une session AVEC le token

**⚠️ Remplacez `VOTRE_TOKEN_ICI` par le token copié à l'étape 1**

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H 'Authorization: Bearer VOTRE_TOKEN_ICI' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Session Bourse 2025",
    "description": "Simulateur boursier pour le cours de 2025",
    "initialBalance": 10000.0,
    "currency": "USD",
    "startDate": "2025-11-10T09:00:00",
    "endDate": "2025-11-17T18:00:00",
    "maxPlayers": 4,
    "allowLateJoin": true
  }'
```

**✅ Réponse attendue : 201 Created**

---

## 🎯 Exemple complet avec un vrai token

```bash
# ÉTAPE 1 : Connexion
TOKEN=$(curl -s -X 'POST' \
  'http://localhost:8084/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin@test.com","password":"Password123!"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token obtenu : ${TOKEN:0:50}..."

# ÉTAPE 2 : Création de session avec le token
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Session Bourse 2025",
    "description": "Simulateur boursier pour le cours de 2025",
    "initialBalance": 10000.0,
    "currency": "USD",
    "startDate": "2025-11-10T09:00:00",
    "endDate": "2025-11-17T18:00:00",
    "maxPlayers": 4,
    "allowLateJoin": true
  }'
```

---

## 🛠️ Scripts automatiques

J'ai créé des scripts pour automatiser le processus :

### Pour Linux/Mac :

```bash
chmod +x test_complet_game_master.sh
./test_complet_game_master.sh
```

### Pour Windows :

```cmd
test_game_master_simple.bat
```

---

## ❌ Pourquoi vous obteniez 401 ?

Votre commande curl **originale** :

```bash
curl -X 'POST' \
  'http://localhost:8084/api/game-master/sessions' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \    # ✅ OK
  -d '{...}'
```

**❌ MANQUE LE HEADER D'AUTHENTIFICATION :**

```bash
-H 'Authorization: Bearer VOTRE_TOKEN'    # ❌ ABSENT !
```

C'est **NORMAL** d'obtenir 401 sans ce header, car l'endpoint est **protégé** !

---

## 🔍 Vérifications avant de tester

### 1. L'application est-elle démarrée ?

```bash
curl http://localhost:8084/actuator/health
```

✅ Doit retourner `{"status":"UP"}`

### 2. L'utilisateur ADMIN existe-t-il ?

```sql
SELECT id, username, email, role FROM players WHERE email = 'admin@test.com';
```

Si non, exécutez :

```bash
mysql -u root -p pibourse < create_admin_user.sql
```

### 3. L'application a-t-elle été redémarrée ?

```bash
# Arrêter l'application (Ctrl+C)
mvn spring-boot:run
```

---

## 📊 Comparaison : Avant vs Après

### ❌ AVANT (votre commande sans token)

```bash
curl -X 'POST' 'http://localhost:8084/api/game-master/sessions' \
  -H 'Content-Type: application/json' \
  -d '{...}'
```

**Résultat : 401 Unauthorized** ❌

### ✅ APRÈS (avec token)

```bash
curl -X 'POST' 'http://localhost:8084/api/game-master/sessions' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -H 'Content-Type: application/json' \
  -d '{...}'
```

**Résultat : 201 Created** ✅

---

## 🎓 Comprendre l'authentification JWT

### Le flux d'authentification

```
1. Client        →  POST /api/auth/login (credentials)
2. Serveur       →  Retourne un TOKEN JWT
3. Client        →  Stocke le TOKEN
4. Client        →  POST /api/game-master/sessions + Header(Authorization: Bearer TOKEN)
5. Serveur       →  Vérifie le TOKEN → Autorise la requête
```

### Sans token

```
1. Client        →  POST /api/game-master/sessions (SANS TOKEN)
2. Serveur       →  401 Unauthorized ❌
```

C'est exactement ce qui vous arrive !

---

## 🚀 Test via Swagger UI (Plus facile)

1. **Ouvrir** : http://localhost:8084/swagger-ui.html

2. **Se connecter** :
   - Endpoint : `POST /api/auth/login`
   - Body :
     ```json
     {
       "username": "admin@test.com",
       "password": "Password123!"
     }
     ```
   - Cliquer **Execute**
   - **Copier le token**

3. **Autoriser** :
   - Cliquer sur **"Authorize"** 🔒 en haut
   - Entrer : `Bearer VOTRE_TOKEN`
   - Cliquer **Authorize**

4. **Créer une session** :
   - Endpoint : `POST /api/game-master/sessions`
   - Entrer les données
   - Cliquer **Execute**
   - ✅ **201 Created**

---

## ✅ Checklist

Avant de tester, vérifiez :

- [ ] ✅ Application démarrée (`mvn spring-boot:run`)
- [ ] ✅ MySQL en cours d'exécution
- [ ] ✅ Utilisateur ADMIN créé (via script SQL)
- [ ] ✅ Corrections appliquées dans `GameMasterController.java`
- [ ] ✅ Application **redémarrée** après les modifications

Puis :

- [ ] ✅ Étape 1 : Connexion réussie
- [ ] ✅ Étape 2 : Token obtenu
- [ ] ✅ Étape 3 : Token utilisé dans la requête
- [ ] ✅ Étape 4 : Session créée (201 Created)

---

## 📞 Si ça ne fonctionne toujours pas

### Erreur : "Token not found in response"

➡️ L'utilisateur n'existe pas ou le mot de passe est incorrect.

**Solution :**
```bash
mysql -u root -p pibourse < create_admin_user.sql
```

### Erreur : "401 Unauthorized" AVEC le token

➡️ L'application n'a pas été redémarrée après les modifications.

**Solution :**
```bash
# Arrêter (Ctrl+C)
mvn spring-boot:run
```

### Erreur : "403 Forbidden"

➡️ Le rôle de l'utilisateur est incorrect.

**Solution :**
```sql
UPDATE players SET role = 'ROLE_ADMIN' WHERE email = 'admin@test.com';
```

---

## 🎉 Résultat final

Une fois les 2 étapes complétées, vous devriez voir :

```json
{
  "id": 1,
  "name": "Session Bourse 2025",
  "description": "Simulateur boursier pour le cours de 2025",
  "status": "CREATED",
  "initialBalance": 10000.0,
  "currency": "USD",
  "startDate": "2025-11-10T09:00:00",
  "endDate": "2025-11-17T18:00:00",
  "maxPlayers": 4,
  "allowLateJoin": true,
  "playerCount": 0,
  "isFull": false
}
```

**✅ Code HTTP : 201 Created**

---

**Date :** 5 novembre 2025  
**Problème :** Erreur 401 car token manquant  
**Solution :** Ajouter le header Authorization avec le token JWT


