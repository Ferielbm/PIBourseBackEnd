# 📧 Guide de Configuration de l'Envoi d'Emails pour la Réinitialisation de Mot de Passe

## 🎯 Objectif
Ce guide explique comment configurer l'envoi d'emails pour la fonctionnalité "Mot de passe oublié" de votre application PiBourse.

---

## 🔍 Diagnostic Initial

Votre code est **correctement implémenté** ! Le problème vient simplement de la configuration SMTP qui utilise des valeurs placeholder.

### Code existant (bien implémenté) ✅
- ✅ `PasswordResetService.java` - Service d'envoi d'emails
- ✅ `AuthController.java` - Endpoint `/api/auth/forgot-password`
- ✅ `JavaMailSender` - Configuré avec Spring Boot Starter Mail
- ✅ Gestion des tokens avec expiration (1 heure par défaut)

---

## 📋 Étape 1 : Créer un Mot de Passe d'Application Gmail

### Pourquoi un "Mot de passe d'application" ?
Gmail ne permet plus d'utiliser votre mot de passe personnel pour les applications tierces. Vous devez créer un mot de passe d'application.

### Instructions détaillées :

1. **Connectez-vous à votre compte Gmail**

2. **Allez dans les paramètres de sécurité** :
   - URL directe : https://myaccount.google.com/security
   - Ou : Compte Google → Sécurité

3. **Activez la validation en 2 étapes** (si ce n'est pas déjà fait) :
   - Faites défiler jusqu'à "Validation en deux étapes"
   - Cliquez sur "Activer"
   - Suivez les instructions

4. **Créez un mot de passe d'application** :
   - Recherchez "Mots de passe des applications" dans la page de sécurité
   - Ou allez directement sur : https://myaccount.google.com/apppasswords
   - Cliquez sur "Sélectionner l'application" → Choisissez "Autre (nom personnalisé)"
   - Entrez le nom : **"PiBourse"** ou **"PiBourse Backend"**
   - Cliquez sur **"Générer"**
   - **⚠️ IMPORTANT** : Copiez le mot de passe de 16 caractères généré (il ressemble à : `abcd efgh ijkl mnop`)
   - **Note** : Retirez les espaces quand vous le copiez dans application.properties

---

## ⚙️ Étape 2 : Configurer application.properties

### Option A : Configuration Gmail (Recommandé pour le développement)

Ouvrez votre fichier `src/main/resources/application.properties` et remplacez les lignes 75-76 :

```properties
# Mail Configuration - Gmail (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-adresse@gmail.com
spring.mail.password=abcdefghijklmnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
```

**Remplacez** :
- `votre-adresse@gmail.com` → Votre vraie adresse Gmail
- `abcdefghijklmnop` → Le mot de passe d'application de 16 caractères (SANS espaces)

### Option B : Configuration Outlook/Hotmail

Si vous préférez utiliser Outlook :

```properties
# Mail Configuration - Outlook (SMTP)
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=votre-adresse@outlook.com
spring.mail.password=votre-mot-de-passe
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp-mail.outlook.com
```

### Option C : Configuration avec un serveur SMTP personnalisé

Si votre entreprise/école a un serveur SMTP :

```properties
# Mail Configuration - SMTP Personnalisé
spring.mail.host=smtp.votre-domaine.com
spring.mail.port=587
spring.mail.username=votre-email@votre-domaine.com
spring.mail.password=votre-mot-de-passe
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Option D : Configuration Mailtrap (Pour les tests)

**Mailtrap** est parfait pour les tests car il capture tous les emails sans les envoyer réellement :

1. Créez un compte gratuit sur https://mailtrap.io
2. Copiez vos credentials depuis l'interface
3. Configurez :

```properties
# Mail Configuration - Mailtrap (Tests)
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=votre-username-mailtrap
spring.mail.password=votre-password-mailtrap
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🔒 Étape 3 : Sécuriser vos Credentials (Important pour la Production)

### ⚠️ NE JAMAIS commiter vos credentials dans Git !

Pour la production, utilisez des variables d'environnement :

```properties
# Mail Configuration - Production (Variables d'environnement)
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

Puis définissez les variables d'environnement :
- Windows PowerShell :
  ```powershell
  $env:MAIL_USERNAME="votre-email@gmail.com"
  $env:MAIL_PASSWORD="votre-mot-de-passe-application"
  ```

- Windows CMD :
  ```cmd
  set MAIL_USERNAME=votre-email@gmail.com
  set MAIL_PASSWORD=votre-mot-de-passe-application
  ```

- Linux/Mac :
  ```bash
  export MAIL_USERNAME="votre-email@gmail.com"
  export MAIL_PASSWORD="votre-mot-de-passe-application"
  ```

---

## ✅ Étape 4 : Tester l'Envoi d'Emails

### 1. Redémarrez votre application Spring Boot

```bash
# Si vous utilisez Maven
mvn spring-boot:run

# Ou si vous avez déjà compilé
java -jar target/PIBourseBackEnd-0.0.1-SNAPSHOT.jar
```

### 2. Testez l'endpoint avec cURL ou Postman

#### Avec cURL :
```bash
curl -X POST http://localhost:8084/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"votre-email-test@gmail.com\"}"
```

#### Avec Postman :
- **Method** : POST
- **URL** : `http://localhost:8084/api/auth/forgot-password`
- **Headers** : `Content-Type: application/json`
- **Body** (raw JSON) :
  ```json
  {
    "email": "votre-email-test@gmail.com"
  }
  ```

### 3. Réponse attendue

```json
{
  "message": "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation."
}
```

### 4. Vérifiez votre boîte email

Vous devriez recevoir un email comme celui-ci :

```
Sujet : Réinitialisation de votre mot de passe - PiBourse

Bonjour,

Vous avez demandé la réinitialisation de votre mot de passe.

Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :
http://localhost:4200/reset-password?token=abc123-xyz789...

Ce lien expirera dans 1 heure(s).

Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.

Cordialement,
L'équipe PiBourse
```

---

## 🔧 Personnalisation (Optionnel)

### Modifier la durée d'expiration du token

Dans `application.properties` (ligne 83) :
```properties
# Par défaut : 1 heure
app.password-reset.token-expiration-hours=1

# Pour 2 heures :
app.password-reset.token-expiration-hours=2

# Pour 30 minutes :
app.password-reset.token-expiration-hours=0.5
```

### Modifier l'URL du frontend

Dans `application.properties` (ligne 84) :
```properties
# URL locale
app.password-reset.frontend-url=http://localhost:4200

# URL de production
app.password-reset.frontend-url=https://votre-domaine.com
```

### Personnaliser l'expéditeur de l'email

Ajoutez dans `application.properties` :
```properties
spring.mail.properties.mail.smtp.from=noreply@pibourse.com
```

Puis modifiez `PasswordResetService.java` (ligne 77) :
```java
message.setFrom("PiBourse <noreply@pibourse.com>");
message.setTo(email);
```

---

## 🐛 Dépannage (Troubleshooting)

### Problème 1 : AuthenticationFailedException

**Erreur** :
```
AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**Solution** :
- Vérifiez que vous utilisez un **mot de passe d'application** (pas votre mot de passe Gmail normal)
- Vérifiez que la validation en 2 étapes est activée
- Retirez les espaces du mot de passe d'application

### Problème 2 : Connection refused

**Erreur** :
```
Could not connect to SMTP host: smtp.gmail.com, port: 587
```

**Solution** :
- Vérifiez votre connexion internet
- Vérifiez que le port 587 n'est pas bloqué par votre firewall
- Essayez le port 465 avec SSL :
  ```properties
  spring.mail.port=465
  spring.mail.properties.mail.smtp.ssl.enable=true
  ```

### Problème 3 : Aucun email reçu (pas d'erreur)

**Solutions** :
1. Vérifiez vos **spams/courriers indésirables**
2. Attendez quelques minutes (parfois il y a un délai)
3. Vérifiez les logs de votre application :
   ```
   INFO  - Password reset email sent to: email@example.com
   ```
4. Utilisez **Mailtrap** pour les tests (Option D)

### Problème 4 : Failed to send password reset email

**Vérifications** :
1. Les credentials sont corrects dans `application.properties`
2. L'application a bien été redémarrée après modification
3. Le compte email existe dans la base de données
4. Consultez les logs complets pour voir l'exception exacte

### Problème 5 : Token invalide ou expiré

**Causes possibles** :
- Le token a plus d'1 heure (expiré)
- Le token a déjà été utilisé
- Le token n'existe pas dans la base de données

**Solution** :
- Demandez un nouveau token via `/api/auth/forgot-password`

---

## 📊 Vérification dans la Base de Données

Pour vérifier que les tokens sont bien créés :

```sql
-- Voir tous les tokens de réinitialisation
SELECT * FROM password_reset_tokens;

-- Voir les tokens non utilisés et non expirés
SELECT * FROM password_reset_tokens 
WHERE used = false 
AND expiry_date > NOW();

-- Supprimer les tokens expirés (nettoyage manuel)
DELETE FROM password_reset_tokens 
WHERE expiry_date < NOW();
```

---

## 🔄 Processus Complet de Test

1. **Inscription** :
   ```bash
   POST /api/auth/register
   {
     "username": "testuser",
     "email": "votre-email@gmail.com",
     "password": "password123"
   }
   ```

2. **Mot de passe oublié** :
   ```bash
   POST /api/auth/forgot-password
   {
     "email": "votre-email@gmail.com"
   }
   ```

3. **Récupérez le token depuis l'email reçu**

4. **Validez le token** (optionnel) :
   ```bash
   GET /api/auth/validate-reset?token=abc123-xyz789...
   ```

5. **Réinitialisez le mot de passe** :
   ```bash
   POST /api/auth/reset-password
   {
     "token": "abc123-xyz789...",
     "newPassword": "nouveauMotDePasse123"
   }
   ```

6. **Connectez-vous avec le nouveau mot de passe** :
   ```bash
   POST /api/auth/login
   {
     "email": "votre-email@gmail.com",
     "password": "nouveauMotDePasse123"
   }
   ```

---

## 📚 Architecture Technique

### Services et Contrôleurs impliqués :

1. **AuthController.java** (lignes 156-169)
   - Endpoint `/api/auth/forgot-password`
   - Appelle `passwordResetService.createPasswordResetToken()`

2. **PasswordResetService.java** (lignes 40-69)
   - Génère un token UUID
   - Sauvegarde le token en base
   - Envoie l'email via `mailSender.send()`

3. **JavaMailSender** (Spring Boot)
   - Bean auto-configuré par `spring-boot-starter-mail`
   - Utilise les propriétés `spring.mail.*`

### Base de données :

Table `password_reset_tokens` :
- `id` : Identifiant unique
- `token` : Token UUID
- `player_id` : Référence vers le joueur
- `expiry_date` : Date d'expiration
- `used` : Booléen indiquant si le token a été utilisé

---

## ✨ Améliorations Futures (Optionnel)

### 1. Template HTML pour les emails

Actuellement, les emails sont en texte brut. Vous pouvez utiliser Thymeleaf pour des emails HTML :

```java
// Utiliser MimeMessageHelper
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true);
helper.setTo(email);
helper.setSubject("Réinitialisation de mot de passe");
helper.setText(htmlContent, true); // true = HTML
```

### 2. Rate Limiting

Pour éviter les abus, limitez les demandes par IP :

```java
// Ajouter un cache avec Guava ou Caffeine
private final Cache<String, Integer> requestCounts;

// Limiter à 3 demandes par heure
if (requestCounts.getIfPresent(email) >= 3) {
    throw new TooManyRequestsException();
}
```

### 3. Notifications par SMS (Twilio)

En complément de l'email :

```xml
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.14.1</version>
</dependency>
```

---

## 📞 Support

Si vous rencontrez des problèmes :

1. **Consultez les logs** : `app-logs.txt` ou la console
2. **Vérifiez la configuration** : Comparez avec ce guide
3. **Testez avec Mailtrap** : Pour isoler le problème
4. **Consultez les erreurs SMTP** : Elles sont généralement explicites

---

## ✅ Checklist Finale

Avant de passer en production :

- [ ] Configuration SMTP correcte dans `application.properties`
- [ ] Mot de passe d'application Gmail créé et testé
- [ ] Variables d'environnement configurées (pas de credentials en dur)
- [ ] Test complet du flux de réinitialisation
- [ ] Email reçu et lien fonctionnel
- [ ] Token expire bien après 1 heure
- [ ] Token ne peut pas être réutilisé
- [ ] Logs vérifiés (pas d'erreurs)
- [ ] Table `password_reset_tokens` peuplée correctement

---

**Dernière mise à jour** : Octobre 2025
**Version** : 1.0

