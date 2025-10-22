# Guide d'Authentification JWT - PIBourse ✅ CORRIGÉ

## 📋 Vue d'ensemble

Ce projet implémente un système d'authentification complet basé sur JWT (JSON Web Token) avec gestion des rôles `ROLE_ADMIN` et `ROLE_PLAYER`.

**✅ Problème 401 résolu** : Le système JWT a été corrigé et fonctionne maintenant correctement avec Spring Security 6.

## 🏗️ Architecture

### Packages créés
```
Player/
├── Controllers/
│   ├── AuthController.java          → Endpoints d'authentification
│   ├── PlayerController.java        → CRUD Players (existant)
│   └── TestSecurityController.java  → Tests d'autorisation
├── Security/
│   ├── JwtUtils.java                → Génération/validation JWT
│   ├── JwtAuthenticationFilter.java → Filtre d'extraction du token
│   ├── UserDetailsImpl.java         → Représentation de l'utilisateur
│   ├── UserDetailsServiceImpl.java  → Chargement des utilisateurs
│   ├── SecurityConfig.java          → Configuration Spring Security
│   ├── AuthEntryPointJwt.java       → Gestion erreurs 401
│   └── dto/
│       ├── LoginRequest.java
│       ├── SignupRequest.java
│       ├── JwtResponse.java
│       └── MessageResponse.java
```

## 🔐 Configuration JWT

### application.properties
```properties
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
jwt.expirationMs=86400000  # 24 heures en millisecondes
```

**Note importante** : Le nom de la propriété est `jwt.expirationMs` (avec Ms à la fin).

## 🚀 Utilisation

### 1. Inscription (Register)

**Endpoint:** `POST /api/auth/register`

**Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "ROLE_PLAYER"
}
```

**Réponse:**
```json
{
  "message": "User registered successfully!"
}
```

### 2. Connexion (Login)

**Endpoint:** `POST /api/auth/login`

**Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Réponse:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "ROLE_PLAYER"
}
```

### 3. Utiliser le Token

Pour accéder aux endpoints protégés, ajoutez le header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 🔒 Règles d'Autorisation

### Endpoints publics (sans authentification)
- `/api/auth/**` - Inscription et connexion
- `/v3/api-docs/**`, `/swagger-ui/**` - Documentation Swagger

### Endpoints par rôle

#### ROLE_ADMIN uniquement
- `/api/admin/**` - Accès administrateur

#### ROLE_PLAYER et ROLE_ADMIN
- `/api/player/**` - Accès joueur

#### Tous les utilisateurs authentifiés
- Tous les autres endpoints nécessitent une authentification

## 🧪 Tester avec Swagger

1. **Démarrez l'application** : `mvn spring-boot:run`
2. **Accédez à Swagger** : `http://localhost:8084/swagger-ui.html`
3. **Suivez le scénario de test ci-dessous**

### Scénario de test complet - ÉTAPE PAR ÉTAPE

#### Étape 1: Créer un ADMIN
```json
POST /api/auth/register
{
  "username": "admin",
  "email": "admin@example.com",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

#### Étape 2: Créer un PLAYER
```json
POST /api/auth/register
{
  "username": "player1",
  "email": "player1@example.com",
  "password": "player123",
  "role": "ROLE_PLAYER"
}
```

#### Étape 3: Se connecter en tant qu'ADMIN
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```
→ Copiez le `token` retourné

#### Étape 4: Tester les endpoints protégés
Dans Swagger, cliquez sur "Authorize" et entrez:
```
Bearer <votre_token>
```

Puis testez:
- ✅ `GET /api/admin/test` - Devrait fonctionner (ADMIN)
- ✅ `GET /api/player/test` - Devrait fonctionner (ADMIN a accès)
- ✅ `GET /api/user/test` - Devrait fonctionner (authentifié)

#### Étape 5: Se connecter en tant que PLAYER
```json
POST /api/auth/login
{
  "username": "player1",
  "password": "player123"
}
```
→ Copiez le nouveau `token`

Testez avec ce token:
- ❌ `GET /api/admin/test` - Devrait échouer (403 Forbidden)
- ✅ `GET /api/player/test` - Devrait fonctionner
- ✅ `GET /api/user/test` - Devrait fonctionner

## 🛡️ Sécurité

### Mot de passe
- Encodés avec **BCrypt** (force 12)
- Jamais stockés en clair
- Validation automatique lors du login

### Token JWT
- Signé avec HMAC-SHA256
- Expire après 24 heures
- Contient le username (subject)
- Validation à chaque requête

### Session
- Mode **STATELESS** (pas de session serveur)
- Token stocké côté client
- CSRF désactivé (pas nécessaire avec JWT)

## 📝 Annotations de sécurité

Vous pouvez utiliser `@PreAuthorize` sur vos méthodes:

```java
@GetMapping("/admin-only")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> adminOnly() {
    return ResponseEntity.ok("Admin content");
}

@GetMapping("/player-or-admin")
@PreAuthorize("hasAnyRole('PLAYER', 'ADMIN')")
public ResponseEntity<String> playerOrAdmin() {
    return ResponseEntity.ok("Player or Admin content");
}
```

## ⚙️ Dépendances ajoutées

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

## ✅ Points clés

1. ✅ Architecture existante préservée
2. ✅ CRUD Player non modifié
3. ✅ Code commenté et structuré
4. ✅ JWT avec expiration configurable
5. ✅ Gestion des rôles ADMIN/PLAYER
6. ✅ Endpoints protégés selon les rôles
7. ✅ BCrypt pour les mots de passe
8. ✅ Compatible Swagger
9. ✅ Gestion d'erreurs (401 Unauthorized)
10. ✅ Session stateless (REST API)

## 🐛 Dépannage

### Erreur 401 Unauthorized
- Vérifiez que le token est valide
- Vérifiez le format: `Authorization: Bearer <token>`
- Vérifiez que le token n'a pas expiré

### Erreur 403 Forbidden
- Vérifiez que votre rôle a accès à l'endpoint
- ADMIN a accès à tout
- PLAYER a accès limité

### Token invalide
- Le token expire après 24h
- Reconnectez-vous pour obtenir un nouveau token

## 📞 Support

Pour toute question, consultez:
- `SecurityConfig.java` - Configuration des autorisations
- `JwtUtils.java` - Logique JWT
- `AuthController.java` - Endpoints d'authentification

