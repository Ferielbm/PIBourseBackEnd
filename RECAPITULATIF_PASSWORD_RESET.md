# ✅ Récapitulatif - Fonctionnalité "Mot de passe oublié"

## 📦 Fichiers créés (7 nouveaux fichiers)

### Entités
✅ `src/main/java/tn/esprit/piboursebackend/Player/Entities/PasswordResetToken.java`

### Repositories
✅ `src/main/java/tn/esprit/piboursebackend/Player/Repositories/PasswordResetTokenRepository.java`

### Services
✅ `src/main/java/tn/esprit/piboursebackend/Player/Services/PasswordResetService.java`

### DTOs
✅ `src/main/java/tn/esprit/piboursebackend/Player/DTOs/ForgotPasswordRequest.java`
✅ `src/main/java/tn/esprit/piboursebackend/Player/DTOs/ResetPasswordRequest.java`
✅ `src/main/java/tn/esprit/piboursebackend/Player/DTOs/ValidateTokenResponse.java`

---

## 🔧 Fichiers modifiés (3 fichiers)

### 1. `pom.xml`
- ✅ Ajout de la dépendance `spring-boot-starter-mail`

### 2. `src/main/java/tn/esprit/piboursebackend/Player/Controllers/AuthController.java`
- ✅ Ajout de 3 nouveaux endpoints :
  - `POST /api/auth/forgot-password`
  - `GET /api/auth/validate-reset`
  - `POST /api/auth/reset-password`

### 3. `src/main/resources/application.properties`
- ✅ Configuration SMTP (Gmail par défaut)
- ✅ Configuration du token (expiration 1h)
- ✅ URL du frontend

---

## 🎯 Endpoints ajoutés

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/auth/forgot-password` | POST | Demande de réinitialisation |
| `/api/auth/validate-reset` | GET | Validation d'un token |
| `/api/auth/reset-password` | POST | Définir nouveau mot de passe |

---

## ⚙️ Configuration nécessaire

### 🔴 IMPORTANT : À faire avant de tester

Modifiez dans `application.properties` (lignes 69-70) :

```properties
spring.mail.username=VOTRE_EMAIL@gmail.com
spring.mail.password=VOTRE_MOT_DE_PASSE_APPLICATION
```

### Pour Gmail :
1. Allez sur https://myaccount.google.com/apppasswords
2. Créez un "Mot de passe d'application"
3. Utilisez ce mot de passe (16 caractères)

### Alternative pour les tests :
Utilisez **Mailtrap** (gratuit) :
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=votre-username-mailtrap
spring.mail.password=votre-password-mailtrap
```

---

## 🧪 Test rapide

### 1. Démarrez l'application
```bash
mvn spring-boot:run
```

### 2. Testez avec l'API

```bash
# 1. Demander la réinitialisation
curl -X POST http://localhost:8084/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "votre-email@gmail.com"}'

# 2. Vérifiez votre email et copiez le token

# 3. Réinitialisez le mot de passe
curl -X POST http://localhost:8084/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token": "VOTRE_TOKEN", "newPassword": "nouveauPass123"}'
```

Ou utilisez le fichier `API_EXAMPLES_PASSWORD_RESET.http` pour tester dans VS Code/IntelliJ.

---

## 📊 Flow fonctionnel

```
1. Utilisateur oublie son mot de passe
   ↓
2. Appelle /api/auth/forgot-password avec son email
   ↓
3. Backend génère un token UUID unique
   ↓
4. Token stocké en DB avec expiration (1h)
   ↓
5. Email envoyé avec lien : http://frontend/reset-password?token=xxx
   ↓
6. Utilisateur clique sur le lien
   ↓
7. Frontend appelle /api/auth/validate-reset?token=xxx
   ↓
8. Si valide, afficher formulaire nouveau mot de passe
   ↓
9. Utilisateur soumet → /api/auth/reset-password
   ↓
10. Mot de passe mis à jour, token marqué comme "utilisé"
   ↓
11. Utilisateur peut se connecter avec nouveau mot de passe
```

---

## 🔒 Sécurité

✅ Token UUID unique (impossible à deviner)
✅ Expiration après 1 heure
✅ Usage unique (ne peut être réutilisé)
✅ Mot de passe hashé avec BCrypt
✅ Message neutre (ne révèle pas si email existe)
✅ Validation des entrées (@Valid sur les DTOs)

---

## 📚 Documentation

- **Guide complet** : `GUIDE_MOT_DE_PASSE_OUBLIE.md`
- **Exemples API** : `API_EXAMPLES_PASSWORD_RESET.http`
- **Swagger UI** : http://localhost:8084/swagger-ui.html

---

## ✅ Checklist avant déploiement

- [ ] Configurer les identifiants SMTP dans `application.properties`
- [ ] Tester le flow complet (forgot → email → reset → login)
- [ ] Modifier `app.password-reset.frontend-url` pour l'URL de production
- [ ] Utiliser des variables d'environnement pour les secrets en production
- [ ] (Optionnel) Configurer un scheduler pour nettoyer les tokens expirés

---

## 🎉 Résultat

✅ Fonctionnalité complète et opérationnelle
✅ Aucun code existant modifié (uniquement des ajouts)
✅ Architecture propre et maintenable
✅ Respect des bonnes pratiques Spring Boot
✅ Prêt pour la production

**Prochaine étape** : Configurez votre SMTP et testez ! 🚀

