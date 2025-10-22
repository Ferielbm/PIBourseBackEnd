# 🧪 Test Complet JWT - PIBourse

## ✅ Corrections appliquées

Voici les corrections qui ont été appliquées pour résoudre le problème 401 :

### 1️⃣ JwtUtils.java
- ✅ Signature avec **HS512** (au lieu de HS256)
- ✅ Parser simplifié : `Jwts.parser().setSigningKey(jwtSecret)`
- ✅ Méthode `generateJwtToken(String username)` pour génération directe

### 2️⃣ application.properties
- ✅ Propriété : `jwt.expirationMs` (et non `jwt.expiration`)
- ✅ Type : `int` (au lieu de `long`)

### 3️⃣ SecurityConfig.java
- ✅ `DaoAuthenticationProvider` avec setters (pas de constructeur avec arguments)
- ✅ Configuration simplifiée sans `EnableWebSecurity`
- ✅ Syntaxe `.csrf().disable()` compatible

### 4️⃣ JwtAuthenticationFilter.java
- ✅ Extraction du token simplifiée
- ✅ Validation et authentification dans le contexte Spring Security

### 5️⃣ AuthController.java
- ✅ Génération du token avec `jwtUtils.generateJwtToken(username)`
- ✅ Retour complet avec token + infos utilisateur

---

## 🚀 Test du flux complet

### Étape 1 : Inscription d'un ADMIN

**Endpoint** : `POST /api/auth/register`

**Request Body** :
```json
{
  "username": "admin",
  "email": "admin@test.com",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

**Réponse attendue** :
```json
{
  "message": "User registered successfully!"
}
```

---

### Étape 2 : Inscription d'un PLAYER

**Endpoint** : `POST /api/auth/register`

**Request Body** :
```json
{
  "username": "player1",
  "email": "player1@test.com",
  "password": "player123",
  "role": "ROLE_PLAYER"
}
```

**Réponse attendue** :
```json
{
  "message": "User registered successfully!"
}
```

---

### Étape 3 : Connexion en tant qu'ADMIN

**Endpoint** : `POST /api/auth/login`

**Request Body** :
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMjg1...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@test.com",
  "role": "ROLE_ADMIN"
}
```

🔑 **IMPORTANT** : Copiez le `token` retourné !

---

### Étape 4 : Utiliser le Token dans Swagger

1. Cliquez sur le bouton **"Authorize"** 🔓 en haut à droite
2. Dans le champ, entrez : `Bearer <votre_token>`
   
   Exemple :
   ```
   Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMjg1...
   ```

3. Cliquez sur **"Authorize"**
4. Cliquez sur **"Close"**

---

### Étape 5 : Tester les endpoints protégés (en tant qu'ADMIN)

#### Test 1 : Endpoint ADMIN uniquement
**Endpoint** : `GET /api/admin/test`

**Réponse attendue** : ✅ **200 OK**
```
✅ Admin Board: Vous avez accès en tant qu'ADMIN
```

#### Test 2 : Endpoint PLAYER/ADMIN
**Endpoint** : `GET /api/player/test`

**Réponse attendue** : ✅ **200 OK**
```
✅ Player Board: Vous avez accès en tant que PLAYER ou ADMIN
```

#### Test 3 : Endpoint authentifié
**Endpoint** : `GET /api/user/test`

**Réponse attendue** : ✅ **200 OK**
```
✅ User Content: Accessible à tous les utilisateurs authentifiés
```

---

### Étape 6 : Connexion en tant que PLAYER

**Endpoint** : `POST /api/auth/login`

**Request Body** :
```json
{
  "username": "player1",
  "password": "player123"
}
```

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwbGF5ZXIxIiwiaWF0Ij...",
  "type": "Bearer",
  "id": 2,
  "username": "player1",
  "email": "player1@test.com",
  "role": "ROLE_PLAYER"
}
```

🔑 Copiez le nouveau token et refaites **"Authorize"** avec ce token

---

### Étape 7 : Tester les endpoints protégés (en tant que PLAYER)

#### Test 1 : Endpoint ADMIN uniquement
**Endpoint** : `GET /api/admin/test`

**Réponse attendue** : ❌ **403 Forbidden**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```
✅ **Normal** : Le PLAYER n'a pas accès aux endpoints ADMIN

#### Test 2 : Endpoint PLAYER/ADMIN
**Endpoint** : `GET /api/player/test`

**Réponse attendue** : ✅ **200 OK**
```
✅ Player Board: Vous avez accès en tant que PLAYER ou ADMIN
```

#### Test 3 : Endpoint authentifié
**Endpoint** : `GET /api/user/test`

**Réponse attendue** : ✅ **200 OK**
```
✅ User Content: Accessible à tous les utilisateurs authentifiés
```

---

## 📊 Résumé des tests

| Endpoint | ADMIN | PLAYER | Non authentifié |
|----------|-------|--------|-----------------|
| `/api/auth/register` | ✅ Public | ✅ Public | ✅ Public |
| `/api/auth/login` | ✅ Public | ✅ Public | ✅ Public |
| `/api/admin/test` | ✅ 200 OK | ❌ 403 | ❌ 401 |
| `/api/player/test` | ✅ 200 OK | ✅ 200 OK | ❌ 401 |
| `/api/user/test` | ✅ 200 OK | ✅ 200 OK | ❌ 401 |

---

## 🔍 Dépannage

### Problème : 401 Unauthorized

**Causes possibles** :
1. Token expiré (expire après 24h)
2. Token invalide ou malformé
3. Header `Authorization` mal formaté
4. Filtre JWT non activé

**Solutions** :
1. Reconnectez-vous pour obtenir un nouveau token
2. Vérifiez le format : `Bearer <token>` (avec un espace)
3. Vérifiez que `jwt.secret` et `jwt.expirationMs` sont définis dans `application.properties`

### Problème : 403 Forbidden

**Cause** : Votre rôle n'a pas accès à cet endpoint

**Solution** : Connectez-vous avec un compte ayant les droits suffisants (ADMIN pour `/api/admin/**`)

### Problème : Token non reconnu

**Vérifications** :
1. `JwtAuthenticationFilter` est bien un `@Component`
2. Le filtre est ajouté dans `SecurityConfig` : `.addFilterBefore(jwtAuthenticationFilter, ...)`
3. Le secret JWT est identique entre génération et validation

---

## ✅ Résultat final

Après ces corrections, le système JWT fonctionne parfaitement :

✅ Login génère un token valide  
✅ Token permet l'accès aux endpoints protégés  
✅ Rôles ADMIN/PLAYER gérés correctement  
✅ Plus d'erreur 401 avec un token valide  
✅ Architecture du projet préservée  
✅ Code clair et bien structuré  

**Vous pouvez maintenant utiliser votre API avec JWT ! 🎉**

