# 🚀 TEST IMMÉDIAT - Étape par étape

## ✅ Corrections appliquées

1. **Supprimé** : Classe `PiBourseApplication.java` en doublon (BUILD FAILURE résolu)
2. **Corrigé** : `AuthController.login()` avec try-catch pour gérer les erreurs d'authentification
3. **Optimisé** : `SecurityConfig` avec ordre correct des requestMatchers
4. **Amélioré** : `JwtAuthenticationFilter` avec `shouldNotFilter()`

---

## 🧪 Tests à effectuer MAINTENANT

### 1️⃣ Démarrer l'application

```bash
mvn spring-boot:run
```

Attendez le message : `Started PiBourseBackEndApplication`

---

### 2️⃣ Test REGISTER (créer un utilisateur)

**Commande curl :**
```bash
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"email\":\"admin@test.com\",\"password\":\"admin123\",\"role\":\"ROLE_ADMIN\"}"
```

**Ou via PowerShell :**
```powershell
Invoke-RestMethod -Uri "http://localhost:8084/api/auth/register" -Method POST -ContentType "application/json" -Body '{"username":"admin","email":"admin@test.com","password":"admin123","role":"ROLE_ADMIN"}'
```

**Résultat attendu :**
```json
{
  "message": "User registered successfully!"
}
```

✅ Si vous avez ce message → Passez au test login
❌ Si erreur → Vérifiez la base de données MySQL

---

### 3️⃣ Test LOGIN (obtenir un token JWT)

**Commande curl :**
```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

**Ou via PowerShell :**
```powershell
Invoke-RestMethod -Uri "http://localhost:8084/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
```

**Résultat attendu :**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@test.com",
  "role": "ROLE_ADMIN"
}
```

✅ **Si vous avez un token → SUCCÈS ! Le 401 est résolu !**
❌ Si 401 → Voir diagnostic ci-dessous

---

### 4️⃣ Test ENDPOINT PROTÉGÉ avec token

Copiez le token obtenu au login, puis :

**Commande curl :**
```bash
curl -X GET http://localhost:8084/api/admin/test \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Ou via PowerShell :**
```powershell
$token = "VOTRE_TOKEN_ICI"
Invoke-RestMethod -Uri "http://localhost:8084/api/admin/test" -Method GET -Headers @{"Authorization"="Bearer $token"}
```

**Résultat attendu :**
```
✅ Admin Board: Vous avez accès en tant qu'ADMIN
```

---

## 🔍 Si vous avez encore un 401 au LOGIN

### Scénario A : Register fonctionne, Login donne 401

**Cause** : Mot de passe mal encodé ou utilisateur non trouvé

**Vérifications :**

1. **Vérifiez la base de données :**
```sql
SELECT id, username, email, password, role FROM player WHERE username='admin';
```

Le mot de passe doit commencer par `$2a$` ou `$2b$` (BCrypt)

2. **Vérifiez les logs de l'application**
Cherchez :
```
Bad credentials
User not found
```

---

### Scénario B : Register ET Login donnent tous les deux 401

**Cause** : SecurityConfig bloque tout

**Solution temporaire pour tester :**

Dans `SecurityConfig.java`, remplacez temporairement :
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // TOUT PUBLIC temporairement
)
```

Si ça fonctionne → Le problème vient des règles de sécurité
Si ça ne fonctionne toujours pas → Le problème vient du controller ou de Spring Boot

---

### Scénario C : 401 avec "Full authentication is required"

**Cause** : Le filtre JWT s'exécute quand même

**Debug** : Ajoutez temporairement dans `JwtAuthenticationFilter.java` :

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    boolean skip = path.startsWith("/api/auth/");
    
    // DEBUG
    System.out.println("🔍 Path: " + path + " → Skip filter: " + skip);
    
    return skip;
}
```

Regardez les logs pour confirmer que le filtre est ignoré pour `/api/auth/login`

---

## 📊 Récapitulatif des modifications

| Fichier | Modification | Raison |
|---------|--------------|--------|
| `PiBourseApplication.java` | ❌ SUPPRIMÉ | Classe main en doublon |
| `AuthController.java` | ✅ try-catch ajouté | Gérer erreurs d'authentification |
| `SecurityConfig.java` | ✅ Ordre requestMatchers | `/api/auth/**` en premier |
| `JwtAuthenticationFilter.java` | ✅ shouldNotFilter() | Ignorer endpoints publics |

---

## ✅ Checklist finale

Avant de tester, vérifiez :

- [ ] MySQL est démarré
- [ ] Base de données `pibourse` existe
- [ ] Application démarre sans erreur
- [ ] Swagger accessible : http://localhost:8084/swagger-ui.html

---

## 🎯 Résultat attendu

✅ **Register** → 200 OK  
✅ **Login** → 200 OK + Token JWT  
✅ **Endpoint protégé** → 200 OK avec token  
❌ **Endpoint protégé sans token** → 401 Unauthorized  

---

## 💡 Utilisez Swagger pour tester facilement

1. Ouvrez : http://localhost:8084/swagger-ui.html
2. Cherchez `auth-controller`
3. Testez `/api/auth/register`
4. Testez `/api/auth/login`
5. Cliquez sur "Authorize" et collez le token
6. Testez les endpoints protégés

---

**Lancez l'application et testez maintenant ! 🚀**

**Reportez-moi le résultat du LOGIN pour que je puisse vous aider davantage si besoin !**

