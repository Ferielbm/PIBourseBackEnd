# 🔐 Test de la Correction JWT - HS512

## ✅ Corrections Effectuées

### 1. **Clé JWT Sécurisée (512 bits)**
- ✅ Ancienne clé : 256 bits (64 caractères hex) - **TROP FAIBLE**
- ✅ Nouvelle clé : **512 bits (104 caractères)** - **CONFORME HS512**
- ✅ Fichier : `application.properties`

### 2. **Utilisation de Keys.hmacShaKeyFor()**
- ✅ Avant : `signWith(SignatureAlgorithm.HS512, String)` - **DÉPRÉCIÉ**
- ✅ Après : `signWith(getSigningKey(), SignatureAlgorithm.HS512)` - **NOUVELLE API**
- ✅ Méthode `getSigningKey()` qui crée une `SecretKey` avec `Keys.hmacShaKeyFor()`

### 3. **Nouvelle API JJWT 0.11.5**
- ✅ Utilisation de `Jwts.parserBuilder()` au lieu de `Jwts.parser()`
- ✅ Compatible avec Spring Boot 3.5.6 et Java 17

### 4. **Logs SLF4J**
- ✅ Remplacement de `System.err.println()` par `logger.error()`
- ✅ Logs informatifs pour chaque opération JWT
- ✅ Messages d'erreur clairs et détaillés

### 5. **Structure Complète**
✅ Méthode `generateJwtToken(String username)`
✅ Méthode `getUserNameFromJwtToken(String token)`
✅ Méthode `validateJwtToken(String token)`

---

## 📝 Explication Technique (Dans le Code)

```java
/**
 * CORRECTION HS512 :
 * L'algorithme HS512 (HMAC-SHA512) nécessite une clé de signature d'au moins 512 bits (64 bytes).
 * Avec l'API JJWT 0.11.5, il faut utiliser Keys.hmacShaKeyFor() qui :
 * - Convertit la chaîne secrète en tableau de bytes
 * - Crée une clé SecretKey adaptée à l'algorithme HS512
 * - Garantit la sécurité cryptographique requise par le standard JWT
 * 
 * L'ancienne méthode signWith(SignatureAlgorithm.HS512, String) est dépréciée car elle 
 * ne garantissait pas la taille minimale de la clé, d'où l'erreur WeakKeyException.
 */
```

---

## 🧪 Tests à Effectuer

### 1. Vérifier que l'application démarre sans erreur
```bash
mvn spring-boot:run
```
✅ Pas d'erreur `WeakKeyException`

### 2. Tester l'inscription (Register)
```bash
curl -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "role": "ROLE_PLAYER"
  }'
```

### 3. Tester la connexion (Login) - **VÉRIFICATION JWT**
```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Résultat Attendu** :
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.SIGNATURE_512_BITS",
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "role": "ROLE_PLAYER"
}
```

### 4. Vérifier les logs
Recherchez dans les logs :
```
✅ JWT token généré avec succès pour l'utilisateur: testuser
```

### 5. Tester avec Swagger UI
1. Ouvrir : http://localhost:8084/swagger-ui/index.html
2. Aller dans `/api/auth/register`
3. Créer un utilisateur
4. Aller dans `/api/auth/login`
5. Se connecter
6. **VÉRIFIER** : Le token JWT est retourné sans erreur `WeakKeyException`

---

## 🎯 Points de Vérification

### ✅ Erreur Corrigée
- ❌ AVANT : `io.jsonwebtoken.security.WeakKeyException: The signing key's size is 384 bits...`
- ✅ APRÈS : Token JWT généré avec succès avec HS512

### ✅ Sécurité
- Clé de 512 bits (conforme au standard HMAC-SHA512)
- API sécurisée avec `Keys.hmacShaKeyFor()`
- Logs clairs en cas d'erreur

### ✅ Compatibilité
- Spring Boot 3.5.6 ✅
- JJWT API 0.11.5 ✅
- Java 17 ✅

---

## 📚 Ressources

- [JJWT GitHub](https://github.com/jwtk/jjwt)
- [RFC 7518 - JSON Web Algorithms (JWA)](https://datatracker.ietf.org/doc/html/rfc7518)
- [HS512 Requirements](https://datatracker.ietf.org/doc/html/rfc7518#section-3.2) : Minimum 512 bits

---

## 🚀 Prochaines Étapes

1. ✅ **Tester en production** avec des clés stockées dans des variables d'environnement
2. ✅ **Rotation des clés JWT** pour plus de sécurité
3. ✅ **Refresh tokens** pour améliorer l'expérience utilisateur

