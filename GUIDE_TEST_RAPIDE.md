# 🚀 GUIDE TEST RAPIDE - Authentification JWT

## ✅ Corrections appliquées

1. ✅ **Port 8084 libéré** (ancien processus arrêté)
2. ✅ **JwtAuthenticationFilter** amélioré avec `getRequestURI()` + logs
3. ✅ **SecurityConfig** finalisé avec endpoints publics
4. ✅ **Table Order** échappée (`@Table(name = "`order`")`)
5. ✅ **AuthController** avec gestion d'erreurs

---

## 🧪 TESTS À EFFECTUER MAINTENANT

L'application démarre... Attendez le message `Started PiBourseBackEndApplication`

---

### 📍 **1. Ouvrir Swagger**

URL : `http://localhost:8084/swagger-ui.html`

✅ **DOIT S'AFFICHER SANS 401**

Dans les logs, vous verrez :
```
🔓 JWT Filter SKIPPED for: /swagger-ui/index.html (public endpoint)
🔓 JWT Filter SKIPPED for: /v3/api-docs/swagger-config (public endpoint)
```

---

### 📝 **2. Test Register**

**Dans Swagger** → Cherchez `auth-controller` → `POST /api/auth/register`

**Body** :
```json
{
  "username": "testadmin",
  "email": "admin@test.com",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

**OU via PowerShell** :
```powershell
Invoke-RestMethod -Uri "http://localhost:8084/api/auth/register" -Method POST -ContentType "application/json" -Body '{"username":"testadmin","email":"admin@test.com","password":"admin123","role":"ROLE_ADMIN"}'
```

✅ **Résultat attendu** : `{"message": "User registered successfully!"}`

**Dans les logs** :
```
🔓 JWT Filter SKIPPED for: /api/auth/register (public endpoint)
```

---

### 🔐 **3. Test Login**

**Dans Swagger** → `POST /api/auth/login`

**Body** :
```json
{
  "username": "testadmin",
  "password": "admin123"
}
```

**OU via PowerShell** :
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8084/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"testadmin","password":"admin123"}'
$response
$token = $response.token
Write-Host "Token reçu : $($token.Substring(0,50))..."
```

✅ **Résultat attendu** : Token JWT retourné

**Réponse** :
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "testadmin",
  "email": "admin@test.com",
  "role": "ROLE_ADMIN"
}
```

**Dans les logs** :
```
🔓 JWT Filter SKIPPED for: /api/auth/login (public endpoint)
```

---

### 🔒 **4. Test endpoint protégé AVEC token**

**Dans Swagger** :
1. Cliquez sur **"Authorize"** 🔓 (en haut à droite)
2. Entrez : `Bearer <collez_votre_token_ici>`
3. Cliquez sur **"Authorize"** puis **"Close"**
4. Testez `/api/admin/test`

**OU via PowerShell** :
```powershell
$headers = @{"Authorization" = "Bearer $token"}
Invoke-RestMethod -Uri "http://localhost:8084/api/admin/test" -Method GET -Headers $headers
```

✅ **Résultat attendu** : 
```
✅ Admin Board: Vous avez accès en tant qu'ADMIN
```

**Dans les logs** :
```
🔒 JWT Filter APPLIED for: /api/admin/test
✅ JWT Authentication successful for user: testadmin
```

---

### 🚫 **5. Test endpoint protégé SANS token**

**Dans Swagger** : Cliquez sur "Authorize" et supprimez le token, puis testez `/api/admin/test`

**OU via PowerShell** :
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8084/api/admin/test" -Method GET
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
}
```

✅ **Résultat attendu** : **401 Unauthorized**

**Dans les logs** :
```
🔒 JWT Filter APPLIED for: /api/admin/test
⚠️ No Bearer token found in Authorization header for: /api/admin/test
```

---

## 📊 Tableau récapitulatif

| Test | Méthode | Auth requise ? | Résultat attendu |
|------|---------|----------------|------------------|
| Swagger UI | GET | ❌ Non | ✅ 200 OK (affiché) |
| Register | POST | ❌ Non | ✅ 200 OK |
| Login | POST | ❌ Non | ✅ 200 OK + JWT |
| /api/admin/test | GET | ✅ Oui + ROLE_ADMIN | ✅ 200 OK |
| /api/admin/test | GET | ❌ Sans token | ✅ 401 Unauthorized |
| /api/player/test | GET | ✅ Oui + ROLE_PLAYER | ✅ 200 OK |

---

## 🔍 Interpréter les logs

### ✅ Logs normaux (tout va bien)

```
🔓 JWT Filter SKIPPED for: /api/auth/login (public endpoint)
🔓 JWT Filter SKIPPED for: /swagger-ui/index.html (public endpoint)
🔒 JWT Filter APPLIED for: /api/admin/test
✅ JWT Authentication successful for user: testadmin
```

### ⚠️ Logs d'erreur (problèmes)

**Si vous voyez** :
```
🔒 JWT Filter APPLIED for: /api/auth/login
```
→ ❌ **Problème** : Le filtre ne devrait PAS s'appliquer sur `/api/auth/login`

**Si vous voyez** :
```
❌ Cannot set user authentication: ...
```
→ ❌ **Problème** : Erreur dans la validation du token ou UserDetailsService

**Si vous voyez** :
```
⚠️ JWT Token validation failed
```
→ ❌ **Problème** : Token invalide ou expiré

---

## 🐛 Dépannage

### Problème : Login retourne toujours 401

**Vérifiez** :
1. Les logs : `🔓 JWT Filter SKIPPED for: /api/auth/login` doit apparaître
2. Si ce n'est PAS le cas : le filtre s'applique quand même
3. **Solution** : Vérifiez que `getRequestURI()` retourne bien `/api/auth/login`

### Problème : Swagger donne 401

**Vérifiez** :
1. Les logs : `🔓 JWT Filter SKIPPED for: /swagger-ui/...` doit apparaître
2. **Solution** : Ajoutez plus de patterns dans `shouldNotFilter()`

### Problème : Token ne fonctionne pas

**Vérifiez** :
1. Format : `Bearer <token>` (avec un espace)
2. Le token n'est pas expiré (24h par défaut)
3. Les logs : `✅ JWT Authentication successful` doit apparaître

---

## 💡 Commandes utiles

### Vérifier que le port est libre
```powershell
netstat -ano | findstr :8084
```

### Arrêter un processus sur le port 8084
```powershell
$pid = (Get-NetTCPConnection -LocalPort 8084).OwningProcess
Stop-Process -Id $pid -Force
```

### Voir les logs en temps réel
Les logs s'affichent dans le terminal Maven

Cherchez :
- `🔓` = Endpoint public (bon signe)
- `🔒` = Endpoint protégé (normal)
- `✅` = Authentification réussie (bon signe)
- `⚠️` = Avertissement
- `❌` = Erreur

---

## ✅ Résultat final attendu

Après tous ces tests, vous devriez avoir :

✅ **Swagger accessible sans 401**  
✅ **Register fonctionne** (200 OK)  
✅ **Login fonctionne** (200 OK + JWT)  
✅ **Endpoint protégé avec token** (200 OK)  
✅ **Endpoint protégé sans token** (401)  
✅ **Logs clairs** avec emojis

---

## 🎉 Félicitations !

Si tous les tests passent, votre système d'authentification JWT est **100% opérationnel** !

**Les logs vous permettent maintenant de voir exactement ce qui se passe à chaque requête.**

---

## 📞 En cas de problème

Si un test échoue :

1. **Regardez les logs** en premier
2. **Cherchez les emojis** : 🔓 🔒 ✅ ⚠️ ❌
3. **Vérifiez** que les endpoints publics ont bien 🔓
4. **Copiez-collez** les logs pertinents pour analyse

**Bonne chance ! 🚀**

