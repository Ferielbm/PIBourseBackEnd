# 📧 Template Email personnalisé (Optionnel)

## Email actuel (Simple)

L'email actuel envoyé est un texte simple. Si vous voulez un email HTML professionnel, voici comment faire.

---

## Option 1 : Email texte simple amélioré

Modifiez la méthode `sendPasswordResetEmail()` dans `PasswordResetService.java` :

```java
message.setText(
    "Bonjour,\n\n" +
    "Vous avez demandé la réinitialisation de votre mot de passe sur PiBourse.\n\n" +
    "Cliquez sur le lien ci-dessous pour créer un nouveau mot de passe :\n" +
    resetLink + "\n\n" +
    "⏰ Ce lien expirera dans " + tokenExpirationHours + " heure(s).\n\n" +
    "🔒 Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n" +
    "   Votre mot de passe actuel reste inchangé.\n\n" +
    "Pour toute question, contactez notre support.\n\n" +
    "Cordialement,\n" +
    "L'équipe PiBourse\n" +
    "https://pibourse.com"
);
```

---

## Option 2 : Email HTML professionnel

### Étape 1 : Ajouter la dépendance Thymeleaf

Ajoutez dans `pom.xml` :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### Étape 2 : Créer le template HTML

Créez le fichier `src/main/resources/templates/email/password-reset.html` :

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .container {
            background: #f9f9f9;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
        }
        .logo {
            font-size: 32px;
            font-weight: bold;
            color: #4CAF50;
        }
        .content {
            background: white;
            padding: 25px;
            border-radius: 8px;
        }
        .button {
            display: inline-block;
            padding: 14px 30px;
            margin: 20px 0;
            background: #4CAF50;
            color: white !important;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
        }
        .button:hover {
            background: #45a049;
        }
        .warning {
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 12px;
            margin: 20px 0;
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            font-size: 12px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="logo">🏦 PiBourse</div>
        </div>
        
        <div class="content">
            <h2>Réinitialisation de mot de passe</h2>
            
            <p>Bonjour,</p>
            
            <p>Vous avez demandé la réinitialisation de votre mot de passe sur <strong>PiBourse</strong>.</p>
            
            <p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
            
            <div style="text-align: center;">
                <a th:href="${resetLink}" class="button">Réinitialiser mon mot de passe</a>
            </div>
            
            <p style="font-size: 12px; color: #666;">
                Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br>
                <a th:href="${resetLink}" th:text="${resetLink}"></a>
            </p>
            
            <div class="warning">
                <strong>⏰ Important :</strong> Ce lien expirera dans <span th:text="${expirationHours}"></span> heure(s).
            </div>
            
            <div class="warning" style="background: #f8d7da; border-color: #dc3545;">
                <strong>🔒 Sécurité :</strong> Si vous n'avez pas demandé cette réinitialisation, 
                ignorez cet email. Votre mot de passe actuel reste inchangé.
            </div>
            
            <p>Pour toute question, contactez notre support à <a href="mailto:support@pibourse.com">support@pibourse.com</a>.</p>
            
            <p>
                Cordialement,<br>
                <strong>L'équipe PiBourse</strong>
            </p>
        </div>
        
        <div class="footer">
            <p>© 2025 PiBourse - Tous droits réservés</p>
            <p>
                <a href="https://pibourse.com">Site web</a> | 
                <a href="https://pibourse.com/privacy">Confidentialité</a> | 
                <a href="https://pibourse.com/contact">Contact</a>
            </p>
        </div>
    </div>
</body>
</html>
```

### Étape 3 : Modifier le service pour utiliser HTML

Remplacez la méthode `sendPasswordResetEmail()` dans `PasswordResetService.java` :

```java
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.MimeMessage;

@Autowired
private TemplateEngine templateEngine;

private void sendPasswordResetEmail(String email, String token) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        
        // Créer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        context.setVariable("expirationHours", tokenExpirationHours);
        
        // Générer le HTML depuis le template
        String htmlContent = templateEngine.process("email/password-reset", context);
        
        helper.setTo(email);
        helper.setSubject("Réinitialisation de votre mot de passe - PiBourse");
        helper.setText(htmlContent, true); // true = HTML
        helper.setFrom("noreply@pibourse.com");
        
        mailSender.send(message);
        log.info("HTML password reset email sent to: {}", email);
    } catch (Exception e) {
        log.error("Failed to send password reset email to: {}", email, e);
        throw new RuntimeException("Failed to send password reset email", e);
    }
}
```

---

## Option 3 : Email avec logo (image)

Si vous voulez inclure un logo :

### 1. Placez votre logo dans `src/main/resources/static/images/logo.png`

### 2. Modifiez le service :

```java
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

// ... autres configurations ...

// Attacher le logo
ClassPathResource logo = new ClassPathResource("static/images/logo.png");
helper.addInline("logo", logo);

// Dans le template HTML :
// <img src="cid:logo" alt="PiBourse" style="width: 150px;" />
```

---

## 🎨 Personnalisation supplémentaire

### Changer les couleurs
- Vert actuel : `#4CAF50`
- Remplacez par votre couleur de marque

### Ajouter votre logo
- Remplacez l'emoji 🏦 par `<img src="logo.png">`

### Multi-langue
Ajoutez un paramètre `locale` et créez plusieurs templates :
- `email/password-reset_en.html`
- `email/password-reset_fr.html`

---

## 📱 Email responsive

Le template HTML fourni est déjà responsive. Testez sur :
- Desktop (Outlook, Gmail web)
- Mobile (Gmail app, iOS Mail)
- Tablette

---

## 🧪 Tester votre email

### Option 1 : Mailtrap (recommandé pour dev)
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
```
Voir les emails sur https://mailtrap.io

### Option 2 : Litmus / Email on Acid
Services payants pour tester le rendu sur tous les clients email.

---

## 📊 Analytics (optionnel)

Pour suivre les clics sur les liens :

```html
<a th:href="${resetLink} + '&utm_source=email&utm_medium=password_reset'" class="button">
```

---

## ✅ Bonnes pratiques

1. ✅ **Sujet clair** : "Réinitialisation de votre mot de passe"
2. ✅ **CTA visible** : Bouton bien visible
3. ✅ **Lien textuel** : Au cas où le bouton ne fonctionne pas
4. ✅ **Expiration claire** : Mentionner 1h
5. ✅ **Rassurer** : "Si vous n'avez pas demandé..."
6. ✅ **Responsive** : Lisible sur mobile
7. ✅ **Pas de spam** : Éviter les mots comme "gratuit", "urgent"

---

## 🚫 À éviter

- ❌ Images trop lourdes (max 100KB par image)
- ❌ JavaScript (ne fonctionne pas dans les emails)
- ❌ CSS externe (inline uniquement)
- ❌ Vidéos (non supportées)
- ❌ Fonts exotiques (utiliser Arial, Helvetica, sans-serif)

---

**Note** : L'email simple (texte) actuel fonctionne parfaitement. 
L'HTML est optionnel et pour une meilleure présentation professionnelle.

