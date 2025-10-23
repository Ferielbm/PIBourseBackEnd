# 🔧 Guide de dépannage - Mot de passe oublié

## ❌ Problème : Email non reçu

### Causes possibles

#### 1. Configuration SMTP incorrecte

**Symptôme** : Erreur dans les logs
```
Failed to send password reset email to: test@example.com
```

**Solution** :
```properties
# Vérifiez application.properties
spring.mail.username=VOTRE_EMAIL
spring.mail.password=VOTRE_MOT_DE_PASSE_APP
```

Pour Gmail :
1. Allez sur https://myaccount.google.com/apppasswords
2. Générez un "Mot de passe d'application" (16 caractères)
3. Utilisez ce mot de passe, PAS votre mot de passe Gmail normal

#### 2. Firewall/Antivirus bloque le port 587

**Solution** :
- Essayez le port 465 (SSL) :
```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

#### 3. Email va dans les spams

**Solution** :
- Vérifiez le dossier spam
- Ajoutez l'expéditeur à vos contacts

#### 4. Gmail "Accès bloqué"

**Solution** :
- Activez la validation en 2 étapes
- Utilisez un mot de passe d'application

---

## ❌ Problème : Token invalide

### Erreur : "Token invalide"

**Causes possibles** :

1. **Token copié incorrectement**
   - Vérifiez qu'il n'y a pas d'espace avant/après
   - Copiez le token complet depuis l'email

2. **Token expiré** (> 1 heure)
   - Redemandez un nouveau token
   - Modifiez `app.password-reset.token-expiration-hours` si besoin

3. **Token déjà utilisé**
   - Chaque token ne peut être utilisé qu'une fois
   - Redemandez un nouveau token

**Solution** :
```http
# Redemander un token
POST http://localhost:8084/api/auth/forgot-password
{
  "email": "votre-email@example.com"
}
```

---

## ❌ Problème : Erreur 500 lors du reset

### Erreur : Internal Server Error

**Causes possibles** :

1. **Base de données non connectée**
```bash
# Vérifiez que MySQL est démarré
# Windows : Services → MySQL → Démarrer
# Mac/Linux : sudo service mysql start
```

2. **Table password_reset_tokens n'existe pas**
```properties
# Dans application.properties, vérifiez :
spring.jpa.hibernate.ddl-auto=update
```

Redémarrez l'application pour créer la table automatiquement.

3. **Player non trouvé**
- L'email doit exister dans la table `players`
- Créez d'abord un compte via `/api/auth/register`

---

## ❌ Problème : Validation échoue (400 Bad Request)

### Erreur : "Password must be at least 6 characters"

**Cause** : Mot de passe trop court

**Solution** :
```json
{
  "token": "votre-token",
  "newPassword": "minimum6caracteres"
}
```

### Erreur : "Email should be valid"

**Cause** : Format d'email invalide

**Solution** :
```json
{
  "email": "format-valide@example.com"
}
```

---

## ❌ Problème : Endpoints non accessibles (404)

### Erreur : 404 Not Found

**Causes possibles** :

1. **Serveur non démarré**
```bash
mvn spring-boot:run
```

2. **Mauvais port**
```bash
# Vérifiez application.properties
server.port=8084
```

URL : http://localhost:8084/api/auth/forgot-password

3. **CORS bloqué** (depuis le frontend)
```java
// Déjà configuré dans AuthController :
@CrossOrigin(origins = "*", maxAge = 3600)
```

---

## ❌ Problème : Connexion échoue après reset

### Symptôme : Nouveau mot de passe ne fonctionne pas

**Vérifications** :

1. **Token marqué comme utilisé ?**
```sql
SELECT * FROM password_reset_tokens 
WHERE token = 'votre-token';
-- Vérifiez que used = true
```

2. **Mot de passe bien mis à jour ?**
```sql
SELECT email, password FROM players 
WHERE email = 'votre-email@example.com';
-- Le hash doit avoir changé
```

3. **Tester la connexion**
```http
POST http://localhost:8084/api/auth/login
{
  "email": "votre-email@example.com",
  "password": "NOUVEAU_mot_de_passe"
}
```

---

## 🧪 Tests de diagnostic

### Test 1 : Vérifier que l'application démarre

```bash
mvn spring-boot:run
```

Recherchez dans les logs :
```
Started PiBourseBackEndApplication in X.XXX seconds
```

### Test 2 : Vérifier la base de données

```sql
-- Connexion MySQL
mysql -u root -p

USE pibourse;

-- Vérifier que les tables existent
SHOW TABLES;

-- Doit afficher :
-- - players
-- - password_reset_tokens
```

### Test 3 : Vérifier les endpoints

```bash
# Vérifier que le serveur répond
curl http://localhost:8084/api/auth/register

# Doit retourner quelque chose (même une erreur 400)
```

### Test 4 : Tester l'envoi d'email (mode debug)

Ajoutez dans `application.properties` :
```properties
logging.level.org.springframework.mail=DEBUG
logging.level.tn.esprit.piboursebackend.Player.Services=DEBUG
```

Relancez et observez les logs lors de l'envoi.

### Test 5 : Vérifier Swagger

Allez sur : http://localhost:8084/swagger-ui.html

Vous devriez voir les 3 nouveaux endpoints :
- POST /api/auth/forgot-password
- GET /api/auth/validate-reset
- POST /api/auth/reset-password

---

## 🔍 Logs utiles

### Activer les logs détaillés

```properties
# application.properties
logging.level.tn.esprit.piboursebackend=DEBUG
logging.level.org.springframework.mail=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Logs à surveiller

**✅ Token créé avec succès :**
```
Password reset token created for user: test@example.com
```

**✅ Email envoyé avec succès :**
```
Password reset email sent to: test@example.com
```

**✅ Mot de passe réinitialisé :**
```
Password successfully reset for user: test@example.com
```

**❌ Erreur d'envoi :**
```
Failed to send password reset email to: test@example.com
```

---

## 🚨 Erreurs communes et solutions rapides

| Erreur | Cause | Solution |
|--------|-------|----------|
| `AuthenticationCredentialsNotFoundException` | Configuration Spring Security | Vérifiez `SecurityConfig.java` |
| `MailSendException` | SMTP mal configuré | Vérifiez username/password |
| `ConnectException: Connection refused` | Serveur SMTP inaccessible | Vérifiez host/port |
| `IllegalArgumentException: Token invalide` | Token incorrect | Copiez le bon token |
| `DataIntegrityViolationException` | Contrainte DB violée | Vérifiez que le player existe |

---

## 🛠️ Solution de dernier recours

### Mode "Email dans les logs" (pour tests)

Si vous ne pouvez vraiment pas configurer SMTP, modifiez temporairement le service :

```java
// Dans PasswordResetService.java
private void sendPasswordResetEmail(String email, String token) {
    String resetLink = frontendUrl + "/reset-password?token=" + token;
    
    log.info("==============================================");
    log.info("📧 EMAIL DE RÉINITIALISATION");
    log.info("==============================================");
    log.info("Destinataire : {}", email);
    log.info("Lien de réinitialisation :");
    log.info("{}", resetLink);
    log.info("Token : {}", token);
    log.info("Expire dans : {} heure(s)", tokenExpirationHours);
    log.info("==============================================");
    
    // Commentez temporairement l'envoi réel
    // mailSender.send(message);
}
```

Vous verrez le token directement dans la console !

---

## ✅ Checklist de vérification

Avant de demander de l'aide, vérifiez :

- [ ] MySQL est démarré
- [ ] Application Spring Boot est démarrée (port 8084)
- [ ] Table `password_reset_tokens` existe
- [ ] Un player avec cet email existe
- [ ] Configuration SMTP est correcte
- [ ] Logs activés (`logging.level.tn.esprit.piboursebackend=DEBUG`)
- [ ] Aucune erreur dans les logs au démarrage
- [ ] Endpoints visibles dans Swagger
- [ ] Token copié correctement (pas d'espaces)
- [ ] Token non expiré (< 1h)

---

## 📞 Besoin d'aide ?

Si le problème persiste :

1. **Activez les logs détaillés**
2. **Reproduisez l'erreur**
3. **Copiez les logs d'erreur complets**
4. **Vérifiez la configuration SMTP**
5. **Testez avec Mailtrap pour isoler le problème SMTP**

---

## 🔗 Ressources

- **Gmail App Passwords** : https://myaccount.google.com/apppasswords
- **Mailtrap (test)** : https://mailtrap.io
- **Spring Mail Docs** : https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email

**Bonne chance ! 🚀**

