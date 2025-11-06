# Guide d'intégration OAuth2 Google - PiBourse Backend

## 🎯 Objectif
Intégrer l'authentification Google OAuth2 dans le backend PiBourse pour permettre aux utilisateurs de se connecter avec leur compte Google.

## 📋 Configuration requise

### 1. Configuration Google OAuth2
- **Client ID** : `413690139100-sdvp...`
- **Client Secret** : `[TON_CLIENT_SECRET]` (à remplacer par le vrai secret)
- **Redirect URI** : `http://localhost:8084/oauth2/callback/google`

### 2. Configuration application.properties
```properties
# OAuth2 Google Configuration
spring.security.oauth2.client.registration.google.client-id=413690139100-sdvp...
spring.security.oauth2.client.registration.google.client-secret=[TON_CLIENT_SECRET]
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8084/oauth2/callback/google
spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code

spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v2/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=id
```

## 🚀 Endpoints disponibles

### 1. Déclencher l'authentification Google
```
GET http://localhost:8084/oauth2/authorization/google
```
**Description** : Redirige vers Google pour l'authentification

### 2. Callback Google (automatique)
```
GET http://localhost:8084/oauth2/callback/google
```
**Description** : Endpoint appelé par Google après authentification. Génère un JWT et redirige vers le frontend.

### 3. Informations utilisateur OAuth2
```
GET http://localhost:8084/oauth2/user
```
**Description** : Récupère les informations de l'utilisateur connecté via OAuth2

## 🧪 Tests avec Postman

### Test 1 : Déclencher l'authentification
1. Ouvrir Postman
2. Créer une requête GET : `http://localhost:8084/oauth2/authorization/google`
3. Cliquer sur "Send"
4. **Résultat attendu** : Redirection vers Google (code 302)

### Test 2 : Test avec navigateur
1. Ouvrir le navigateur
2. Aller sur : `http://localhost:8084/oauth2/authorization/google`
3. **Résultat attendu** : Redirection vers Google OAuth2
4. Se connecter avec un compte Google
5. **Résultat attendu** : Redirection vers le frontend avec JWT

### Test 3 : Vérifier les informations utilisateur
1. Après authentification, appeler : `http://localhost:8084/oauth2/user`
2. **Résultat attendu** : JSON avec les informations utilisateur et JWT

## 📝 Fonctionnalités implémentées

### ✅ Services créés
- **OAuth2UserService** : Gestion des utilisateurs OAuth2
  - Création automatique d'utilisateurs
  - Mise à jour des informations existantes
  - Génération de JWT

### ✅ Controllers créés
- **OAuth2Controller** : Gestion des endpoints OAuth2
  - `/oauth2/authorization/google` : Déclencher l'auth
  - `/oauth2/callback/google` : Callback Google
  - `/oauth2/user` : Informations utilisateur

### ✅ Configuration Security
- Intégration OAuth2 dans SecurityConfig
- Endpoints OAuth2 publics
- Gestion des sessions stateless

## 🔧 Gestion des utilisateurs

### Nouvel utilisateur Google
1. L'utilisateur se connecte avec Google
2. Le système vérifie si l'email existe
3. Si non, création d'un nouveau Player avec :
   - Email Google
   - Nom Google (ou email si pas de nom)
   - Rôle PLAYER
   - Mot de passe vide (OAuth2)

### Utilisateur existant
1. L'utilisateur se connecte avec Google
2. Le système trouve l'utilisateur par email
3. Mise à jour des informations si nécessaire
4. Génération du JWT

## 🎯 Flux d'authentification

```
1. Utilisateur → http://localhost:8084/oauth2/authorization/google
2. Redirection → Google OAuth2
3. Utilisateur → Se connecte sur Google
4. Google → http://localhost:8084/oauth2/callback/google
5. Backend → Traite l'utilisateur (création/connexion)
6. Backend → Génère JWT
7. Backend → Redirige vers frontend avec JWT
```

## 🚨 Points d'attention

### Configuration Google Console
- Vérifier que l'URI de redirection est bien configurée
- S'assurer que le Client Secret est correct
- Tester avec différents comptes Google

### Base de données
- Les utilisateurs OAuth2 ont un mot de passe vide
- L'email doit être unique
- Le rôle par défaut est PLAYER

### Sécurité
- JWT généré avec la même clé que l'auth normale
- Sessions stateless
- Validation des tokens Google

## 🐛 Dépannage

### Erreur "Client ID not found"
- Vérifier la configuration dans application.properties
- S'assurer que le Client ID est correct

### Erreur "Redirect URI mismatch"
- Vérifier l'URI dans Google Console
- S'assurer que l'URI correspond exactement

### Erreur "Invalid client secret"
- Vérifier le Client Secret
- S'assurer qu'il n'y a pas d'espaces en début/fin

### Problème de redirection
- Vérifier que le frontend est accessible
- Tester avec Postman d'abord

## 📊 Monitoring

### Logs à surveiller
- Création d'utilisateurs OAuth2
- Erreurs d'authentification Google
- Génération de JWT
- Redirections

### Métriques importantes
- Nombre de connexions OAuth2
- Taux d'erreur d'authentification
- Temps de réponse des callbacks

## 🎉 Prochaines étapes

1. **Tester l'intégration** avec Postman et navigateur
2. **Configurer le frontend** pour recevoir le JWT
3. **Implémenter la déconnexion** OAuth2
4. **Ajouter d'autres providers** (Facebook, GitHub, etc.)
5. **Améliorer la gestion d'erreurs** et les messages utilisateur
