# ⚡ Quick Start - Mot de passe oublié (2 minutes)

## 🎯 Ce que vous devez faire MAINTENANT

### 1️⃣ Configurer l'email (30 secondes)

Ouvrez `src/main/resources/application.properties` et modifiez les lignes 69-70 :

```properties
spring.mail.username=VOTRE_EMAIL@gmail.com
spring.mail.password=VOTRE_MOT_DE_PASSE_APP
```

#### Comment obtenir le mot de passe d'application Gmail ?

1. Allez sur : https://myaccount.google.com/apppasswords
2. Créez un nouveau mot de passe d'application
3. Copiez-le (16 caractères genre : `abcd efgh ijkl mnop`)
4. Collez-le dans `spring.mail.password`

---

### 2️⃣ Démarrer l'application (30 secondes)

```bash
mvn spring-boot:run
```

Attendez de voir :
```
Started PiBourseBackEndApplication in X.XXX seconds
```

---

### 3️⃣ Tester (1 minute)

#### Option A : Avec VS Code / IntelliJ

1. Ouvrez `API_EXAMPLES_PASSWORD_RESET.http`
2. Cliquez sur "Send Request" à côté de chaque requête

#### Option B : Avec cURL

```bash
# 1. Demander réinitialisation
curl -X POST http://localhost:8084/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"votre-email@gmail.com"}'

# 2. Vérifier votre email → copier le token

# 3. Réinitialiser
curl -X POST http://localhost:8084/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"LE_TOKEN_RECU","newPassword":"nouveauPass123"}'
```

---

## ✅ C'est fait !

Vous avez maintenant :
- ✅ 3 nouveaux endpoints fonctionnels
- ✅ Envoi d'email automatique
- ✅ Système de token sécurisé

---

## 🚨 Problème ?

### Email non reçu ?
1. Vérifiez le dossier spam
2. Vérifiez que vous utilisez un "Mot de passe d'application" Gmail

### Autre problème ?
Consultez `TROUBLESHOOTING_PASSWORD_RESET.md`

---

## 📚 Documentation complète

- **Guide complet** : `GUIDE_MOT_DE_PASSE_OUBLIE.md`
- **Exemples API** : `API_EXAMPLES_PASSWORD_RESET.http`
- **Dépannage** : `TROUBLESHOOTING_PASSWORD_RESET.md`

---

**Bonne utilisation ! 🎉**

