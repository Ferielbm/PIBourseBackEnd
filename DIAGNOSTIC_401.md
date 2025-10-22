# 🔍 Diagnostic approfondi du 401

## Tests à effectuer immédiatement

### 1. Vérifier que l'application démarre sans erreur

Regardez les logs au démarrage. Cherchez :
- ✅ `Started PiBourseBackEndApplication` 
- ❌ Erreurs de configuration Spring Security
- ❌ Erreurs de bean autowiring

### 2. Tester register D'ABORD (pour créer un utilisateur)

```bash
curl -v -X POST http://localhost:8084/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@test.com",
    "password": "password123",
    "role": "ROLE_ADMIN"
  }'
```

**Si register fonctionne (200 OK)** : Passez au test login
**Si register échoue (401)** : Le problème est dans SecurityConfig

### 3. Tester login ENSUITE

```bash
curl -v -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Analysez la réponse détaillée avec `-v`**

---

## Scénarios possibles et solutions

### Scénario 1 : Register fonctionne, Login échoue avec 401

**Cause** : Le filtre JWT ou l'AuthenticationManager bloque le login

**Solution** :
1. Vérifier que `shouldNotFilter()` retourne bien `true` pour `/api/auth/login`
2. Vérifier que le user existe en DB avec le bon mot de passe encodé
3. Vérifier que `AuthenticationManager` est correctement configuré

### Scénario 2 : Register ET Login échouent avec 401

**Cause** : SecurityConfig bloque TOUS les endpoints `/api/auth/**`

**Solution** :
1. L'ordre des `requestMatchers()` est critique
2. `/api/auth/**` DOIT être AVANT les autres règles
3. Le filtre JWT DOIT ignorer `/api/auth/**`

### Scénario 3 : 401 avec message "Full authentication is required"

**Cause** : Le filtre JWT s'exécute malgré `shouldNotFilter()`

**Solution** :
1. Vérifier que `shouldNotFilter()` est bien appelé
2. Ajouter des logs dans `shouldNotFilter()` pour debug
3. Vérifier qu'il n'y a pas d'autres filtres de sécurité

### Scénario 4 : 401 avec message "Bad credentials"

**Cause** : Le login est bien accessible, mais l'authentification échoue

**Solution** :
1. Vérifier que le mot de passe en DB est bien encodé en BCrypt
2. Vérifier que le username existe
3. Vérifier que `UserDetailsService` charge bien l'utilisateur

---

## Checklist de debugging

### Dans SecurityConfig.java
- [ ] `/api/auth/**` est en `permitAll()` 
- [ ] `/api/auth/**` est AVANT les autres règles
- [ ] `SessionCreationPolicy.STATELESS` est configuré
- [ ] `AuthenticationManager` bean existe
- [ ] `PasswordEncoder` bean existe
- [ ] Le filtre JWT est ajouté AVANT `UsernamePasswordAuthenticationFilter`

### Dans JwtAuthenticationFilter.java
- [ ] `shouldNotFilter()` existe et retourne `true` pour `/api/auth/**`
- [ ] `doFilterInternal()` a un try-catch pour ne pas bloquer en cas d'erreur
- [ ] Le filtre ne lance pas d'exception pour les endpoints publics

### Dans AuthController.java
- [ ] `@RequestMapping("/api/auth")` est présent
- [ ] `@PostMapping("/login")` est présent  
- [ ] `AuthenticationManager` est autowired
- [ ] `PasswordEncoder` est autowired
- [ ] `JwtUtils` est autowired

### Dans la base de données
- [ ] La table `player` existe
- [ ] Un utilisateur de test existe
- [ ] Le mot de passe est encodé en BCrypt (commence par `$2a$` ou `$2b$`)
- [ ] Le rôle est bien défini (`ROLE_ADMIN` ou `ROLE_PLAYER`)

---

## Commande de debug ultime

Ajoutez ceci temporairement dans `JwtAuthenticationFilter` :

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getServletPath();
    boolean skip = path.startsWith("/api/auth/") || 
                   path.startsWith("/v3/api-docs/") || 
                   path.startsWith("/swagger-ui/");
    
    // DEBUG - À RETIRER EN PRODUCTION
    System.out.println("🔍 shouldNotFilter() - Path: " + path + " → Skip: " + skip);
    
    return skip;
}
```

Et dans `doFilterInternal()` :

```java
@Override
protected void doFilterInternal(...) {
    // DEBUG - À RETIRER EN PRODUCTION
    System.out.println("🚨 doFilterInternal() - Path: " + request.getServletPath());
    
    // ... reste du code
}
```

Regardez les logs pour voir :
- Si `shouldNotFilter()` est appelé pour `/api/auth/login`
- Si `doFilterInternal()` s'exécute quand même

---

## Si RIEN ne fonctionne

Essayez cette configuration **ULTRA SIMPLE** dans SecurityConfig :

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()  // TOUT est public temporairement
        );
    
    return http.build();
}
```

Si ça fonctionne :
- Le problème vient de la configuration de sécurité
- Rajoutez les règles une par une

Si ça ne fonctionne toujours pas :
- Le problème vient du controller ou de la configuration Spring Boot
- Vérifiez les logs de démarrage

---

## Logs à vérifier

Cherchez dans les logs :

### Signe de succès
```
Mapping [...] to tn.esprit.piboursebackend.Player.Controllers.AuthController.authenticateUser
```

### Signe de problème
```
Filter 'jwtAuthenticationFilter' configured successfully
No mapping found for HTTP request with URI [/api/auth/login]
Access Denied
```

---

## Prochaines étapes

1. **Démarrez l'application** avec les modifications
2. **Testez register** en premier
3. **Testez login** ensuite  
4. **Regardez les logs** attentivement
5. **Reportez-moi** :
   - Le résultat du register (200 ou 401 ?)
   - Le résultat du login (200 ou 401 ?)
   - Les logs si erreur

Je vous aiderai à identifier précisément le problème ! 🔍

