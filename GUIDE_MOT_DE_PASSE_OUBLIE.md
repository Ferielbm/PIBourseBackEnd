# 🔐 Guide - Fonctionnalité "Mot de passe oublié"

## 📋 Vue d'ensemble

Cette fonctionnalité permet aux utilisateurs de réinitialiser leur mot de passe via email en 3 étapes simples.

---

## 🏗️ Architecture ajoutée

### 1️⃣ Entités
- **`PasswordResetToken`** : Stocke les tokens de réinitialisation avec expiration (1h par défaut)

### 2️⃣ Repositories
- **`PasswordResetTokenRepository`** : Gestion des tokens en base de données

### 3️⃣ Services
- **`PasswordResetService`** : Logique métier complète (génération token, envoi email, validation, reset)

### 4️⃣ DTOs
- **`ForgotPasswordRequest`** : Requête avec email
- **`ResetPasswordRequest`** : Requête avec token + nouveau mot de passe
- **`ValidateTokenResponse`** : Réponse de validation de token

### 5️⃣ Endpoints REST
Tous ajoutés dans `AuthController` :

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/forgot-password` | Demande de réinitialisation |
| GET | `/api/auth/validate-reset?token=xxx` | Validation du token |
| POST | `/api/auth/reset-password` | Réinitialisation du mot de passe |

---

## 🔄 Flow fonctionnel complet

### Étape 1 : Demande de réinitialisation
```http
POST http://localhost:8084/api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Réponse :**
```json
{
  "message": "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation."
}
```

**Ce qui se passe :**
1. Un token UUID est généré
2. Le token est stocké en base avec expiration (1h)
3. Un email est envoyé avec le lien de réinitialisation

---

### Étape 2 : Validation du token (optionnel)
```http
GET http://localhost:8084/api/auth/validate-reset?token=abc123-xyz789
```

**Réponse si valide :**
```json
{
  "valid": true,
  "message": "Token valide",
  "email": "user@example.com"
}
```

**Réponse si invalide/expiré :**
```json
{
  "valid": false,
  "message": "Ce token a expiré"
}
```

---

### Étape 3 : Réinitialisation du mot de passe
```http
POST http://localhost:8084/api/auth/reset-password
Content-Type: application/json

{
  "token": "abc123-xyz789",
  "newPassword": "nouveauMotDePasse123"
}
```

**Réponse en cas de succès :**
```json
{
  "message": "Mot de passe réinitialisé avec succès!"
}
```

**Réponse en cas d'erreur :**
```json
{
  "message": "Erreur: Ce token a expiré"
}
```

---

## ⚙️ Configuration requise

### 1. Configuration email dans `application.properties`

Vous devez configurer un serveur SMTP. Voici les options :

#### Option A : Gmail
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-application
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Pour Gmail :**
1. Activez la validation en 2 étapes sur votre compte Google
2. Générez un "Mot de passe d'application" : https://myaccount.google.com/apppasswords
3. Utilisez ce mot de passe dans `spring.mail.password`

#### Option B : Outlook/Hotmail
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=votre-email@outlook.com
spring.mail.password=votre-mot-de-passe
```

#### Option C : Mailtrap (pour tests en développement)
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=votre-username-mailtrap
spring.mail.password=votre-password-mailtrap
```

### 2. Configuration personnalisée

```properties
# Durée de validité du token (en heures, par défaut 1h)
app.password-reset.token-expiration-hours=1

# URL de votre frontend (pour le lien dans l'email)
app.password-reset.frontend-url=http://localhost:4200
```

---

## 🧪 Tests avec Postman/HTTP Client

### Test complet du flow

```http
### 1. Créer un utilisateur (si nécessaire)
POST http://localhost:8084/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

### 2. Demander la réinitialisation
POST http://localhost:8084/api/auth/forgot-password
Content-Type: application/json

{
  "email": "test@example.com"
}

### 3. Vérifier vos emails et copier le token

### 4. Valider le token (optionnel)
GET http://localhost:8084/api/auth/validate-reset?token=VOTRE_TOKEN_ICI

### 5. Réinitialiser le mot de passe
POST http://localhost:8084/api/auth/reset-password
Content-Type: application/json

{
  "token": "VOTRE_TOKEN_ICI",
  "newPassword": "nouveauPassword123"
}

### 6. Se connecter avec le nouveau mot de passe
POST http://localhost:8084/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "nouveauPassword123"
}
```

---

## 🔒 Sécurité

### Bonnes pratiques implémentées :

1. **Token unique UUID** : Impossible à deviner
2. **Expiration temporelle** : 1 heure par défaut
3. **Usage unique** : Le token ne peut être utilisé qu'une seule fois
4. **Message neutre** : Ne révèle pas si l'email existe (sécurité)
5. **Mot de passe hashé** : BCrypt via `PasswordEncoder`
6. **Validation des entrées** : Annotations `@Valid` sur les DTOs

---

## 🗄️ Structure de la base de données

Nouvelle table créée automatiquement :

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    player_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);
```

---

## 🧹 Maintenance

### Nettoyage automatique des tokens expirés

Le service fournit une méthode `cleanupExpiredTokens()` que vous pouvez programmer :

```java
// Exemple : Ajoutez dans une classe de configuration
@Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
public void scheduleTokenCleanup() {
    passwordResetService.cleanupExpiredTokens();
}
```

---

## 🐛 Troubleshooting

### Problème : Email non reçu

**Solutions :**
1. Vérifiez vos identifiants SMTP dans `application.properties`
2. Pour Gmail : utilisez un "Mot de passe d'application"
3. Vérifiez les logs : `logging.level.tn.esprit.piboursebackend=DEBUG`
4. Testez avec Mailtrap en développement

### Problème : Token invalide/expiré

**Solutions :**
1. Le token expire après 1h par défaut
2. Vérifiez que le token n'a pas déjà été utilisé
3. Redemandez un nouveau token via `/forgot-password`

### Problème : AuthenticationException

**Solutions :**
1. Assurez-vous que Spring Security autorise ces endpoints
2. Ces endpoints doivent être accessibles sans authentification

---

## 📦 Dépendances ajoutées

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 📝 Notes importantes

1. **Aucun code existant n'a été modifié** - Uniquement des ajouts
2. **Architecture respectée** - Suit le pattern du projet
3. **Swagger intégré** - Documentation API automatique disponible sur `/swagger-ui.html`
4. **Prêt pour la production** - Remplacez les configs SMTP par des variables d'environnement

---

## 🎯 Frontend - Exemple d'intégration

### Formulaire "Mot de passe oublié"
```typescript
async forgotPassword(email: string) {
  const response = await fetch('http://localhost:8084/api/auth/forgot-password', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  });
  
  const data = await response.json();
  alert(data.message);
}
```

### Page de réinitialisation
```typescript
async resetPassword(token: string, newPassword: string) {
  const response = await fetch('http://localhost:8084/api/auth/reset-password', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token, newPassword })
  });
  
  const data = await response.json();
  if (response.ok) {
    alert('Mot de passe réinitialisé avec succès!');
    // Rediriger vers la page de connexion
  } else {
    alert(data.message);
  }
}
```

---

## ✅ Checklist de déploiement

- [ ] Configurer les identifiants SMTP réels
- [ ] Modifier `app.password-reset.frontend-url` pour l'URL de production
- [ ] Utiliser des variables d'environnement pour les secrets
- [ ] Tester le flow complet en production
- [ ] Configurer un scheduler pour nettoyer les tokens expirés
- [ ] Personnaliser le template d'email si nécessaire

---

**🎉 Votre fonctionnalité "Mot de passe oublié" est maintenant opérationnelle !**

