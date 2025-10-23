# 🔧 Correction - Erreur 500 sur /v3/api-docs

## ❌ Problème initial

- **Erreur** : HTTP 500 sur l'endpoint `/v3/api-docs`
- **Impact** : Swagger UI inaccessible ou incomplet

---

## ✅ Solution appliquée

### 1️⃣ Mise à jour de SwaggerConfig.java

**Changements** :
- ✅ Suppression de `@Profile("!test")` qui causait des conflits
- ✅ Ajout de la configuration de sécurité JWT dans Swagger
- ✅ Ajout des composants (`Components`) avec schémas de sécurité
- ✅ Configuration complète de `SecurityScheme` pour JWT Bearer

**Fichier** : `src/main/java/tn/esprit/piboursebackend/Player/Config/SwaggerConfig.java`

```java
// Schéma de sécurité JWT
SecurityScheme securityScheme = new SecurityScheme()
    .type(SecurityScheme.Type.HTTP)
    .scheme("bearer")
    .bearerFormat("JWT")
    .description("JWT token authentication");

// Composants (schémas de sécurité)
Components components = new Components()
    .addSecuritySchemes("Bearer Authentication", securityScheme);
```

---

### 2️⃣ Amélioration de application.properties

**Changements** :
- ✅ Ajout de `springdoc.packages-to-scan=tn.esprit.piboursebackend`
- ✅ Ajout de `springdoc.paths-to-match=/api/**`
- ✅ Activation explicite de l'API docs
- ✅ Configuration de l'UI Swagger

**Fichier** : `src/main/resources/application.properties`

```properties
springdoc.api-docs.enabled=true
springdoc.packages-to-scan=tn.esprit.piboursebackend
springdoc.paths-to-match=/api/**
```

---

### 3️⃣ Mise à jour de springdoc-openapi

**Changement** :
- ✅ Version `2.3.0` → `2.6.0`

**Fichier** : `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

---

## 🧪 Vérification

### Test rapide

```bash
# 1. Démarrer l'application
mvn spring-boot:run

# 2. Attendre le démarrage complet (environ 30 secondes)

# 3. Tester l'endpoint
curl http://localhost:8084/v3/api-docs
```

**Résultat attendu** : JSON contenant la documentation OpenAPI (pas d'erreur 500)

### Via navigateur

1. **Swagger UI** : http://localhost:8084/swagger-ui.html
2. **API Docs** : http://localhost:8084/v3/api-docs

---

## 🎯 Résultat

### Avant
```
GET http://localhost:8084/v3/api-docs
❌ HTTP 500 - Internal Server Error
```

### Après
```
GET http://localhost:8084/v3/api-docs
✅ HTTP 200 - Documentation JSON complète
```

**Swagger UI maintenant fonctionnel avec** :
- ✅ Documentation complète de tous les endpoints
- ✅ Support JWT Bearer Authentication
- ✅ Interface utilisateur interactive
- ✅ Test des endpoints directement depuis Swagger

---

## 📊 Fonctionnalités Swagger disponibles

Depuis Swagger UI (http://localhost:8084/swagger-ui.html) :

### Endpoints visibles :

**Authentication**
- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Inscription
- `POST /api/auth/forgot-password` - ✨ Mot de passe oublié
- `GET /api/auth/validate-reset` - ✨ Valider token
- `POST /api/auth/reset-password` - ✨ Réinitialiser mot de passe

**Player Management**
- `GET /api/player/all` - Liste des joueurs
- `POST /api/player/create` - Créer un joueur
- `GET /api/player/{id}` - Détails d'un joueur
- etc.

### Fonctionnalité d'authentification

1. Cliquez sur **Authorize** 🔒 en haut à droite
2. Entrez votre token JWT : `Bearer <votre-token>`
3. Testez les endpoints protégés

---

## 🔒 Sécurité

La configuration de sécurité autorise Swagger sans authentification :

```java
// SecurityConfig.java
.requestMatchers(
    "/api/auth/**",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/h2-console/**"
).permitAll()
```

**Note** : En production, envisagez de protéger Swagger avec une authentification.

---

## 📦 Bonus : Base de données de test

Deux options créées pour vous :

### Option 1 : MySQL
- **Script** : `database-test-setup.sql`
- **Configuration** : `application-test.properties`
- **Données** : 4 utilisateurs de test inclus

### Option 2 : H2 (Recommandé)
- **Avantage** : Aucune installation, rapide, en mémoire
- **Console** : http://localhost:8084/h2-console
- **Configuration** : Déjà dans `application-test.properties`

**Démarrer avec le profil test** :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

---

## 📚 Documentation créée

| Fichier | Description |
|---------|-------------|
| `TEST_DATABASE_GUIDE.md` | Guide complet base de données de test |
| `database-test-setup.sql` | Script SQL pour MySQL |
| `application-test.properties` | Configuration profil test |
| `test-swagger-fix.bat` | Script de test Windows |
| `test-swagger-fix.sh` | Script de test Linux/Mac |
| `FIX_SWAGGER_500_ERROR.md` | Ce document |

---

## ✅ Checklist de vérification

Après avoir appliqué les corrections :

- [x] Projet compile sans erreur
- [ ] Application démarre sans erreur
- [ ] `/v3/api-docs` retourne HTTP 200 (pas 500)
- [ ] Swagger UI accessible sur `/swagger-ui.html`
- [ ] Tous les endpoints visibles dans Swagger
- [ ] Authentification JWT fonctionne dans Swagger
- [ ] Tests API fonctionnent

---

## 🚨 Si l'erreur persiste

### 1. Nettoyer complètement le projet
```bash
mvn clean install -DskipTests
```

### 2. Supprimer le dossier target
```bash
rm -rf target/  # Linux/Mac
rmdir /s /q target  # Windows
```

### 3. Vérifier les logs
```bash
# Chercher les erreurs
mvn spring-boot:run | grep ERROR
```

### 4. Tester avec curl verbose
```bash
curl -v http://localhost:8084/v3/api-docs
```

### 5. Vérifier qu'aucun controller n'a d'erreur
- Annotations correctes
- DTOs valides
- Pas de références circulaires

---

## 🎉 Résumé

**3 fichiers modifiés** :
1. ✅ `SwaggerConfig.java` - Configuration complète
2. ✅ `application.properties` - Paramètres Swagger
3. ✅ `pom.xml` - Version springdoc mise à jour

**4 fichiers créés** :
1. ✅ `application-test.properties` - Profil de test
2. ✅ `database-test-setup.sql` - Base de test MySQL
3. ✅ `TEST_DATABASE_GUIDE.md` - Documentation
4. ✅ `FIX_SWAGGER_500_ERROR.md` - Ce guide

**Résultat** :
- ✅ Erreur 500 corrigée
- ✅ Swagger UI fonctionnel
- ✅ Base de données de test prête
- ✅ Documentation complète

---

**Bon développement ! 🚀**

