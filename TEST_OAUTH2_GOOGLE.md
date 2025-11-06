# Test OAuth2 Google - PiBourse Backend

## 🚀 Démarrage rapide

### 1. Configuration des propriétés
Assurez-vous que dans `application.properties`, vous avez remplacé :
```properties
spring.security.oauth2.client.registration.google.client-secret=[TON_CLIENT_SECRET]
```
Par votre vrai Client Secret de Google.

### 2. Démarrage de l'application
```bash
mvn spring-boot:run
```

### 3. Tests à effectuer

#### Test 1 : Vérifier que l'application démarre
- L'application doit démarrer sur le port 8084
- Aucune erreur de compilation

#### Test 2 : Test avec Postman
1. **Requête GET** : `http://localhost:8084/oauth2/authorization/google`
2. **Résultat attendu** : Code 302 (redirection vers Google)

#### Test 3 : Test avec navigateur
1. Ouvrir : `http://localhost:8084/oauth2/authorization/google`
2. **Résultat attendu** : Redirection vers Google OAuth2
3. Se connecter avec un compte Google
4. **Résultat attendu** : Redirection vers le frontend avec JWT

#### Test 4 : Vérifier Swagger
1. Ouvrir : `http://localhost:8084/swagger-ui.html`
2. **Résultat attendu** : Documentation Swagger avec les endpoints OAuth2

## 🔍 Endpoints à tester

### Endpoints OAuth2
- `GET /oauth2/authorization/google` - Déclencher l'auth Google
- `GET /oauth2/callback/google` - Callback Google (automatique)
- `GET /oauth2/user` - Informations utilisateur

### Endpoints existants (doivent toujours fonctionner)
- `POST /api/auth/login` - Login classique
- `POST /api/auth/register` - Inscription classique
- `GET /api/player/**` - Endpoints protégés

## 🐛 Dépannage

### Erreur "Client ID not found"
- Vérifier la configuration Google Console
- S'assurer que le Client ID est correct dans application.properties

### Erreur "Redirect URI mismatch"
- Vérifier l'URI dans Google Console : `http://localhost:8084/oauth2/callback/google`
- S'assurer qu'il n'y a pas de slash final

### Erreur de compilation
- Vérifier que toutes les dépendances sont installées : `mvn clean install`
- Redémarrer l'application

### Problème de redirection
- Vérifier que le frontend est accessible sur `http://localhost:4200`
- Tester d'abord avec Postman

## 📊 Vérifications

### Base de données
- Vérifier qu'un nouvel utilisateur est créé dans la table `players`
- L'email doit être celui du compte Google
- Le rôle doit être `ROLE_PLAYER`
- Le mot de passe doit être vide

### JWT
- Le JWT généré doit être valide
- Il doit contenir l'email de l'utilisateur
- Il doit être utilisable pour les endpoints protégés

### Logs
- Vérifier les logs de création d'utilisateur
- Vérifier les logs de génération JWT
- Aucune erreur dans les logs

## ✅ Checklist de validation

- [ ] Application démarre sans erreur
- [ ] Endpoint `/oauth2/authorization/google` redirige vers Google
- [ ] Authentification Google fonctionne
- [ ] Callback `/oauth2/callback/google` fonctionne
- [ ] Utilisateur créé en base de données
- [ ] JWT généré et valide
- [ ] Redirection vers frontend avec JWT
- [ ] Endpoints existants toujours fonctionnels
- [ ] Swagger accessible et à jour

## 🎯 Prochaines étapes

1. **Tester avec différents comptes Google**
2. **Vérifier la gestion des utilisateurs existants**
3. **Tester la déconnexion**
4. **Intégrer avec le frontend Angular**
5. **Ajouter la gestion d'erreurs avancée**
