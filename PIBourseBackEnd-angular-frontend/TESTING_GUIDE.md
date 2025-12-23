# 🧪 Guide de Test Complet

## Phase 1: Vérification de la Connexion WebSocket

### Étape 1.1: Démarrer l'Application

```bash
cd PIBourseBackEnd-angular-frontend
npm start
```

Application démarre sur: `http://localhost:4200`

### Étape 1.2: Vérifier les Logs

Ouvrir **DevTools** (F12) → **Console** et attendre:

```
✅ WebSocket connecté
🔗 WebSocket connecté depuis Shell
```

Si vous voyez ces logs, la connexion WebSocket est établie ✅

### Étape 1.3: Vérifier le Tab Network (Optionnel)

DevTools → Network:
- Chercher "ws" dans le filtre
- Vous devez voir `ws://localhost:8084/ws` avec status **101 Switching Protocols**

---

## Phase 2: Tester les Alertes Existantes

### Étape 2.1: Créer une Alerte

1. Cliquer sur **"Alertes Prix"** dans le menu
2. Remplir le formulaire:
   - **Symbole:** AAPL
   - **Prix Min:** 150
   - **Prix Max:** 155
3. Cliquer **"Créer Alerte"**

Résultat attendu: 
```
✅ Alerte créée avec succès!
```

### Étape 2.2: Vérifier dans la Console

```javascript
// La alerte doit charger dans la liste
// Console log: "Symboles chargés (alert-market): Array(50)"
```

---

## Phase 3: Tester Manuellement l'Exécution d'Ordre (Backend)

### Prérequis

Le backend doit avoir:
- ✅ WebSocket configuré
- ✅ L'endpoint de test pour l'exécution manuelle

### Étape 3.1: Créer un Ordre de Test (Frontend)

1. Aller à **"Salle de Marché"**
2. Sélectionner **AAPL**
3. Créer un ordre:
   - **Type:** BUY
   - **Quantité:** 100
   - **Prix:** 150
4. Cliquer **"Placer l'ordre"**

Résultat attendu:
```
Toast: ✅ Ordre créé avec succès
```

### Étape 3.2: Déclencher Manuellement l'Exécution (Backend)

```bash
# Via Postman ou curl
POST http://localhost:8084/api/players/4/orders/{orderId}/execute-manual

Body (JSON):
{
  "executedPrice": 151.5,
  "executedQuantity": 100
}

Response:
{
  "message": "✅ Ordre exécuté et notifications publiées"
}
```

### Étape 3.3: Vérifier les Notifications Frontend

Après avoir déclenché l'exécution, vous devez voir:

**Si AUCUNE alerte ne correspond:**
```
Toast ✅ (Success):
  "✅ Ordre exécuté: AAPL BUY @ 151.5€"
  Durée: 5 secondes
```

**Si une alerte correspond (151.5 dans [150, 155]):**
```
Toast ✅ (Success):
  "✅ Ordre exécuté: AAPL BUY @ 151.5€"
  
Toast 🚨 (Warning):
  "🚨 ALERTE: AAPL @ 151.50€ dans [150.00, 155.00]"
  Durée: ∞ (l'utilisateur ferme)
```

---

## Phase 4: Tests Automatisés (Matching Engine)

### Étape 4.1: Créer Deux Ordres Opposés

**Ordre 1 - Buy**
1. Aller à "Salle de Marché"
2. Créer:
   - Type: **BUY**
   - Symbole: **AAPL**
   - Quantité: 100
   - Prix: 150

**Ordre 2 - Sell**
1. Créer (depuis la même page):
   - Type: **SELL**
   - Symbole: **AAPL**
   - Quantité: 100
   - Prix: 150 (ou inférieur au Buy)

### Étape 4.2: Matching Automatique

Le backend doit automatiquement:
1. Détecter les ordres opposés
2. Exécuter le matching
3. Publier les notifications WebSocket

### Étape 4.3: Vérifier les Toasts

Frontend doit afficher:
```
Toast ✅ 1: "Ordre exécuté: AAPL BUY @ 150€"
Toast ✅ 2: "Ordre exécuté: AAPL SELL @ 150€"
Toast 🚨 3: "ALERTE: AAPL @ 150€ dans [150, 155]" (si alerte existe)
```

---

## Phase 5: Tests Edge Cases

### Test 1: Reconnexion WebSocket

1. Ouvrir DevTools → Network
2. Throttle: Sélectionner "Offline"
3. Attendre 5 secondes
4. Throttle: Sélectionner "Online"

Résultat attendu:
```
Console:
  ❌ Erreur connexion WebSocket
  [Retry après 5s...]
  ✅ WebSocket reconnecté
```

### Test 2: Alerte Hors Plage

Créer une alerte pour AAPL [160, 165] et exécuter un ordre à 151.5:

Résultat attendu:
```
Toast ✅: "Ordre exécuté: AAPL BUY @ 151.5€"
(PAS de toast 🚨 car 151.5 ∉ [160, 165])
```

### Test 3: Alerte Inactive

1. Créer alerte: AAPL [150, 155]
2. Mettre le statut à **PAUSED** ou **CANCELLED**
3. Exécuter ordre à 151.5

Résultat attendu:
```
Toast ✅: "Ordre exécuté..."
(PAS de toast 🚨 car alerte n'est pas ACTIVE)
```

### Test 4: Multiples Toasts

1. Créer 3 alertes: AAPL [140,150], MSFT [370,390], TSLA [240,260]
2. Créer 3 ordres exécutés simultanément

Résultat attendu:
```
Les toasts s'empilent en haut à droite, chacun avec animation
```

---

## Phase 6: Vérification Console Logs

Tous les logs importants doivent apparaître dans la console:

```javascript
// Démarrage
"✅ WebSocket connecté"
"🔗 WebSocket connecté depuis Shell"
"Symboles chargés: Array(50)"

// Notification reçue
"📊 Ordre exécuté reçu: {...}"
"🔔 Alerte déclenchée (prix atteint): {...}"

// Erreurs (optionnel)
"❌ Erreur connexion WebSocket: ..."
"Erreur vérification alertes: ..."
```

---

## 📊 Checklist de Validation

### ✅ Connexion WebSocket
- [ ] Log "✅ WebSocket connecté" visible
- [ ] Tab Network montre `ws://localhost:8084/ws` avec 101
- [ ] Console sans erreurs au démarrage

### ✅ Toasts Visibles
- [ ] Toast s'affiche en haut à droite
- [ ] Animation d'entrée smooth
- [ ] Les couleurs changent selon le type (vert, orange, rouge)

### ✅ Notifications d'Ordres
- [ ] Toast ✅ apparait quand ordre exécuté
- [ ] Texte contient symbole, côté et prix
- [ ] Disparait après 5 secondes

### ✅ Notifications d'Alertes
- [ ] Toast 🚨 apparait quand alerte correspond
- [ ] Texte contient symbole et plage de prix
- [ ] Reste visible jusqu'au clic (durée ∞)

### ✅ Vérification des Alertes
- [ ] Alerte ACTIVE seule déclenche notification
- [ ] Prix hors plage ne déclenche pas notification
- [ ] Alerte avec symbole différent ne déclenche pas notification

### ✅ Gestion des Erreurs
- [ ] Perte de connexion → retry auto
- [ ] Reconnexion réussie → logs affichent reconnexion

---

## 🛠️ Troubleshooting

### Problème 1: Aucun Log WebSocket

**Cause possible:** Backend n'est pas démarré

**Solution:**
```bash
# Vérifier que le backend écoute sur 8084
lsof -i :8084
# ou
netstat -an | grep 8084
```

### Problème 2: Toast n'Apparait Pas

**Cause possible:** Shell component n'a pas chargé le ToastContainerComponent

**Solution:**
```typescript
// Vérifier dans shell.component.ts:
imports: [..., ToastContainerComponent]
template: `<app-toast-container></app-toast-container>`
```

### Problème 3: Alerte ne se Déclenche Pas

**Cause possible:**
1. Alerte n'est pas ACTIVE
2. Symbole ne correspond pas
3. Prix hors plage

**Debug:**
```javascript
// Dans console, vérifier les alertes chargées
// Puis vérifier le prix de l'exécution
console.log('Alertes chargées:', alerts);
console.log('Prix exécuté:', executedPrice);
```

### Problème 4: Toast Disparait Immédiatement

**Cause possible:** Duration = 0 ou très court

**Solution:** Vérifier dans `toast.service.ts`
```typescript
// Pour les alertes, duration doit être 0 ou undefined:
toastService.warning('Message', 0);  // ✅ Correct
```

---

## 📱 Test sur Mobile/Tablet

### Via Navigateur
```
Chrome DevTools → Device Emulation → Sélectionner device
```

Vérifier:
- [ ] Toast visible et lisible
- [ ] Pas de débordement d'écran
- [ ] Animations fluides
- [ ] WebSocket fonctionne

### Via Network Throttle
```
Chrome DevTools → Network → Throttle: "Slow 4G"
```

Vérifier:
- [ ] WebSocket se reconnecte proprement
- [ ] Toasts affichent à temps
- [ ] Pas de lag de l'UI

---

## 🎬 Scénario Complet de Test

### Scénario: Trading avec Alertes

**Préparation:**
```
1. Créer 3 alertes: AAPL [150,155], MSFT [375,385], TSLA [240,250]
2. Créer 3 ordres: BUY AAPL 100@151, BUY MSFT 50@380, BUY TSLA 100@245
```

**Exécution:**
```
Backend déclenche le matching pour tous les ordres
```

**Résultat Attendu:**
```
Frontend affiche 6 toasts:
  ✅ Ordre exécuté: AAPL BUY @ 151€
  🚨 ALERTE: AAPL @ 151€ dans [150, 155]
  ✅ Ordre exécuté: MSFT BUY @ 380€
  🚨 ALERTE: MSFT @ 380€ dans [375, 385]
  ✅ Ordre exécuté: TSLA BUY @ 245€
  🚨 ALERTE: TSLA @ 245€ dans [240, 250]
```

---

## 📈 Métriques de Performance

### Temps de Réaction Attendus

| Action | Temps |
|--------|-------|
| Réception WebSocket | < 100ms |
| Parsing JSON | < 1ms |
| Vérification alertes | < 50ms |
| Rendu toast | < 16ms |
| Total latence | < 200ms |

### Ressources

| Ressource | Valeur |
|-----------|--------|
| Mémoire (idle) | ~2-3 MB |
| Mémoire (100 alertes) | ~4-5 MB |
| CPU (recevoir 10 msg/s) | < 5% |
| Bande passante (1 msg) | ~200 bytes |

---

## 🎓 Apprentissage du Flux

Pour bien comprendre le système:

1. **Démarrer l'app** → Voir les logs WebSocket
2. **Créer une alerte** → Voir le chargement dans la liste
3. **Exécuter un ordre** → Voir les 2 toasts s'afficher
4. **Fermer un toast** → Cliquer sur le ✕
5. **Perdre connexion** → Observer la reconnexion

---

## ✅ Test Validation Finale

Une fois tous les tests passés, cocher:

- [ ] WebSocket connecté et reconnectant
- [ ] Toasts visibles et animés
- [ ] Alertes se déclenchent correctement
- [ ] Pas d'erreur console
- [ ] Performance acceptable
- [ ] Mobile-friendly

🎉 **Si tout est OK, l'implémentation est RÉUSSIE!**

---

**Dernière mise à jour:** 22 novembre 2025
