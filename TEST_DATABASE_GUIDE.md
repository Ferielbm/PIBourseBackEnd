# 🧪 Guide - Base de données de test

## 🎯 Objectif

Créer une base de données séparée pour les tests sans affecter vos données de développement.

---

## 📦 Option 1 : Base de données MySQL de test

### Étape 1 : Créer la base de données

```bash
# Connexion MySQL
mysql -u root -p

# Exécuter le script
source database-test-setup.sql
```

Ou directement :
```bash
mysql -u root -p < database-test-setup.sql
```

### Étape 2 : Créer un profil de test

Le fichier `application-test.properties` a déjà été créé pour vous.

### Étape 3 : Démarrer avec le profil de test

```bash
# Avec Maven
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Ou définir la variable d'environnement
export SPRING_PROFILES_ACTIVE=test
mvn spring-boot:run
```

### Données de test disponibles

| Username | Email | Password | Rôle |
|----------|-------|----------|------|
| admin | admin@pibourse.test | password123 | ROLE_ADMIN |
| player1 | player1@pibourse.test | password123 | ROLE_PLAYER |
| player2 | player2@pibourse.test | password123 | ROLE_PLAYER |
| testuser | test@pibourse.test | password123 | ROLE_PLAYER |

---

## 📦 Option 2 : Base de données H2 en mémoire (Recommandé pour tests)

### Avantages
✅ Aucune installation requise
✅ Rapide
✅ Nettoyage automatique
✅ Idéal pour les tests

### Utilisation

1. **Démarrer avec le profil test** :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

2. **Accéder à la console H2** :
   - URL : http://localhost:8084/h2-console
   - JDBC URL : `jdbc:h2:mem:pibourse_test`
   - Username : `sa`
   - Password : (laisser vide)

3. **Tester l'API** avec les mêmes endpoints

---

## 🔧 Résoudre l'erreur 500 sur /v3/api-docs

### Problème résolu ✅

Les changements suivants ont été apportés :

1. **SwaggerConfig.java** mis à jour avec :
   - Configuration de sécurité JWT
   - Composants correctement définis
   - Suppression du `@Profile("!test")`

2. **application.properties** amélioré avec :
   - `springdoc.packages-to-scan=tn.esprit.piboursebackend`
   - `springdoc.paths-to-match=/api/**`
   - Configuration complète de Swagger UI

3. **SecurityConfig.java** (déjà correct) :
   - `/v3/api-docs/**` autorisé
   - `/swagger-ui/**` autorisé

### Vérification

```bash
# 1. Démarrer l'application
mvn spring-boot:run

# 2. Tester l'endpoint
curl http://localhost:8084/v3/api-docs

# 3. Accéder à Swagger UI
# http://localhost:8084/swagger-ui.html
```

**Résultat attendu** : Documentation JSON complète de l'API (pas d'erreur 500)

---

## 🧪 Tests rapides

### Test 1 : Vérifier Swagger

```bash
# Devrait retourner du JSON (pas d'erreur 500)
curl http://localhost:8084/v3/api-docs

# Devrait retourner du HTML
curl http://localhost:8084/swagger-ui.html
```

### Test 2 : Connexion avec utilisateur de test

```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "player1@pibourse.test",
    "password": "password123"
  }'
```

### Test 3 : Réinitialisation de mot de passe

```bash
curl -X POST http://localhost:8084/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "player1@pibourse.test"
  }'
```

---

## 📝 Configuration dans pom.xml

La version de springdoc a été mise à jour vers 2.6.0 :

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

---

## 🔄 Basculer entre dev et test

### Mode développement (MySQL)
```bash
# Utilise application.properties par défaut
mvn spring-boot:run
```

### Mode test (H2)
```bash
# Utilise application-test.properties
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

---

## 🐛 Dépannage

### Erreur 500 persiste sur /v3/api-docs ?

1. **Nettoyer et recompiler** :
```bash
mvn clean install
```

2. **Vérifier les logs** :
```bash
tail -f logs/spring-boot-logger.log
```

3. **Vérifier qu'aucun controller n'a d'erreur** :
   - Annotations Swagger correctes
   - DTOs valides
   - Pas de références circulaires

4. **Tester avec curl** :
```bash
curl -v http://localhost:8084/v3/api-docs
```

5. **Désactiver temporairement la sécurité** :
Dans `SecurityConfig.java`, changer temporairement :
```java
.anyRequest().permitAll()  // Au lieu de .authenticated()
```

### Base de données MySQL non accessible ?

1. **Vérifier que MySQL est démarré** :
```bash
# Windows
net start MySQL80

# Linux/Mac
sudo service mysql start
```

2. **Vérifier les credentials** :
```properties
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

3. **Créer la base manuellement** :
```sql
CREATE DATABASE pibourse_test;
```

---

## ✅ Checklist

Après avoir suivi ce guide :

- [ ] Base de données de test créée (MySQL ou H2)
- [ ] Application démarre sans erreur
- [ ] `/v3/api-docs` retourne du JSON (pas d'erreur 500)
- [ ] `/swagger-ui.html` accessible
- [ ] Login fonctionne avec utilisateur de test
- [ ] Réinitialisation de mot de passe fonctionne

---

## 🎉 Résultat

Vous avez maintenant :
- ✅ Base de données de test isolée
- ✅ Swagger/OpenAPI fonctionnel (pas d'erreur 500)
- ✅ Données de test prêtes
- ✅ Deux profils (dev et test)

**Profitez de vos tests ! 🚀**

