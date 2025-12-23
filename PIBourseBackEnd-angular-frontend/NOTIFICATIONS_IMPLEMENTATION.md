# 🔔 Implémentation des Notifications WebSocket - Alertes Exécution d'Ordres

## 📋 Vue d'ensemble

Ce document décrit l'implémentation côté frontend pour :
1. **Écouter les exécutions d'ordres** via WebSocket
2. **Vérifier automatiquement** si elles correspondent à des alertes prix
3. **Déclencher les notifications** de manière asynchrone

---

## 🏗️ Architecture Frontend

### Services Créés

#### 1. `WebsocketService` 
**Fichier:** `src/app/services/websocket.service.ts`

Gère la connexion WebSocket avec reconnexion automatique.

```typescript
// Connexion WebSocket
connect(): Promise<void>
subscribe(destination: string, callback: (message: any) => void): () => void
disconnect(): void
isConnected(): boolean
```

**Points clés:**
- Utilise SockJS + Stomp
- Endpoint: `http://localhost:8084/ws`
- Reconnexion automatique en cas de déconnexion
- Gère plusieurs souscriptions simultanément

#### 2. `OrderExecutionNotificationService`
**Fichier:** `src/app/services/order-execution-notification.service.ts`

Orchestre l'écoute des ordres et la vérification des alertes.

**Méthodes principales:**

```typescript
// Initialiser pour un joueur
initializeForPlayer(playerId: number): void

// Événements observables
orderExecution$: Observable<OrderExecutionNotification>
alertTriggered$: Observable<AlertTriggeredNotification>
```

**Flux d'exécution:**

```
WebSocket /topic/scheduled-orders/{playerId}
    ↓
OrderExecutionNotification reçue
    ↓
Émet sur orderExecution$
    ↓
Vérifie les alertes correspondantes (checkAndTriggerAlerts)
    ↓
Si prix dans plage → AlertTriggeredNotification
    ↓
Émet sur alertTriggered$
```

---

## 🔌 Configuration Backend Requise

### 1. **Endpoint WebSocket Principal**

```yaml
URL: ws://localhost:8084/ws (SockJS)
ou: wss://localhost:8084/ws (secure)
```

### 2. **Topic Exécution d'Ordres**

Publier les exécutions d'ordres sur:

```
/topic/scheduled-orders/{playerId}
```

**Payload JSON:**
```json
{
  "orderId": 123,
  "symbol": "AAPL",
  "side": "BUY",
  "executedPrice": 150.25,
  "executedQuantity": 100,
  "remainingQuantity": 0,
  "status": "FILLED",
  "executedAt": "2025-11-22T10:30:00Z"
}
```

### 3. **Topic Alertes (Optionnel)**

Pour les alertes déclenchées directement par le backend:

```
/topic/alerts/{playerId}
```

**Payload JSON:**
```json
{
  "alertId": 456,
  "symbol": "AAPL",
  "alertType": "PRICE_REACHED",
  "minPrice": 150.0,
  "maxPrice": 155.0,
  "currentPrice": 150.25,
  "triggeredAt": "2025-11-22T10:30:00Z"
}
```

---

## 🎯 Flux d'Intégration Backend

### Étape 1: Exécution d'Ordre

Quand un ordre est exécuté (PENDING → OPEN ou PARTIAL):

```java
@Service
public class OrderExecutionService {
  
  @Autowired
  private SimpMessagingTemplate messagingTemplate;
  
  public void executeOrder(Order order, double executedPrice, int executedQty) {
    // 1. Mettre à jour l'ordre dans la BDD
    order.setStatus(OrderStatus.FILLED);
    order.setExecutedPrice(executedPrice);
    order.setRemainingQuantity(order.getQuantity() - executedQty);
    orderRepository.save(order);
    
    // 2. Publier l'événement sur WebSocket
    OrderExecutionDto notification = new OrderExecutionDto(
      order.getId(),
      order.getSymbol(),
      order.getSide(),
      executedPrice,
      executedQty,
      order.getRemainingQuantity(),
      order.getStatus().toString(),
      LocalDateTime.now()
    );
    
    messagingTemplate.convertAndSend(
      "/topic/scheduled-orders/" + order.getPlayerId(),
      notification
    );
  }
}
```

### Étape 2: Vérification des Alertes (Frontend)

Le frontend automatiquement:

1. **Reçoit** la notification via WebSocket
2. **Charge** les alertes ACTIVE du joueur
3. **Compare** le prix exécuté avec les plages des alertes
4. **Déclenche** une notification si match

```typescript
// Code frontend automatique:
private checkAndTriggerAlerts(playerId: number, execution: OrderExecutionNotification): void {
  // Charge les alertes
  this.alertService.getAlerts(playerId).subscribe({
    next: (alerts: PriceAlert[]) => {
      // Vérifie chaque alerte
      for (const alert of alerts) {
        if (alert.symbol === execution.symbol && alert.status === 'ACTIVE') {
          // Vérifie la plage de prix
          if (this.isPriceInAlertRange(
            execution.executedPrice,
            alert.minPrice,
            alert.maxPrice
          )) {
            // Déclenche la notification
            this.alertTriggeredNotifications.next({...});
          }
        }
      }
    }
  });
}
```

---

## 🎨 Interface Utilisateur

### Composant ToastContainer

**Fichier:** `src/app/components/toast-container/toast-container.component.ts`

Affiche les notifications en haut à droite de l'écran:

```
┌─────────────────────────────────┐
│ ✅ Ordre exécuté: AAPL BUY...   │
├─────────────────────────────────┤
│ 🚨 ALERTE: AAPL @ 150.25€...    │
└─────────────────────────────────┘
```

**Types:**
- ✅ Success (vert) - Exécutions d'ordres
- ⚠️ Warning (orange) - Alertes prix atteintes
- ❌ Error (rouge) - Erreurs
- ℹ️ Info (bleu) - Informations

### Service Toast

**Fichier:** `src/app/services/toast.service.ts`

```typescript
toastService.success('Message');    // 5s par défaut
toastService.warning('Message');    // 5s par défaut
toastService.error('Message');      // 5s par défaut
toastService.info('Message');       // 5s par défaut
```

---

## 📡 Initialisation dans Shell

**Fichier:** `src/app/layout/shell/shell.component.ts`

Au démarrage de l'application:

```typescript
ngOnInit(): void {
  // 1. Demander permission notifications navigateur
  this.notificationService.requestNotificationPermission();

  // 2. Connecter WebSocket
  this.websocketService.connect().then(() => {
    // 3. Initialiser écoute pour playerId = 4
    this.notificationService.initializeForPlayer(4);
    
    // 4. Écouter les exécutions d'ordres
    this.notificationService.orderExecution$.subscribe(execution => {
      // Afficher toast de succès
      this.toastService.success(`Ordre exécuté: ${execution.symbol}...`);
    });
    
    // 5. Écouter les alertes déclenchées
    this.notificationService.alertTriggered$.subscribe(alert => {
      // Afficher toast d'alerte urgente (sans timeout)
      this.toastService.warning(`ALERTE: ${alert.symbol}...`, 0);
    });
  });
}
```

---

## 🔧 Configuration Spring Boot Backend

### 1. Ajouter la dépendance WebSocket/Stomp

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. Configurer WebSocket

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
  }
}
```

### 3. Publier les notifications

```java
@Service
public class OrderService {
  
  @Autowired
  private SimpMessagingTemplate messagingTemplate;
  
  public void onOrderMatched(Order order, ExecutionDetails details) {
    // Publier sur le topic
    messagingTemplate.convertAndSend(
      "/topic/scheduled-orders/" + order.getPlayerId(),
      new OrderExecutionNotification(
        order.getId(),
        order.getSymbol(),
        order.getSide(),
        details.executedPrice,
        details.executedQuantity,
        order.getRemainingQuantity(),
        order.getStatus(),
        Instant.now()
      )
    );
  }
}
```

---

## 🧪 Test d'Intégration

### 1. Vérifier la connexion WebSocket

Ouvrir DevTools (F12) → Console:
```javascript
// Chercher les messages "✅ WebSocket connecté"
console.log affichera: "✅ WebSocket connecté depuis Shell"
```

### 2. Tester l'exécution d'ordre

Backend:
```bash
# Exécuter un ordre via le matching engine
POST /api/players/4/orders/match
Body: { "orderId": 123, "executedPrice": 150.25, ... }
```

Frontend:
```
✅ Toast devrait apparître: "Ordre exécuté: AAPL BUY @ 150.25€"
```

### 3. Tester les alertes

Backend:
```bash
# Exécuter un ordre qui correspond à une alerte
# (symbole AAPL, prix entre minPrice et maxPrice)
```

Frontend:
```
🚨 Toast devrait apparître: "ALERTE: AAPL @ 150.25€ dans [150.0, 155.0]"
```

---

## ⚠️ Points Importants

### Gestion du PlayerId

**Actuellement:** `playerId = 4` est hardcodé dans le Shell.

**À améliorer:** Créer un service `CurrentPlayerService`:

```typescript
@Injectable({ providedIn: 'root' })
export class CurrentPlayerService {
  private playerId = new BehaviorSubject<number>(4);
  playerId$ = this.playerId.asObservable();
  
  setPlayerId(id: number): void {
    this.playerId.next(id);
  }
}
```

### Format des Timestamps

Les alertes doivent avoir le même timestamp que l'exécution:

```typescript
"executedAt": "2025-11-22T10:30:00Z"  // ✅ ISO8601
"executedAt": 1700637000000            // Timestamp en ms acceptable
```

### Gestion des Erreurs WebSocket

Si la connexion échoue:

```typescript
// Le frontend retry automatiquement
// Logs dans console:
// "❌ Erreur connexion WebSocket: ..."
```

---

## 📝 Résumé des Fichiers Créés/Modifiés

| Fichier | Type | Description |
|---------|------|-------------|
| `websocket.service.ts` | Créé | Gestion connexion WebSocket |
| `order-execution-notification.service.ts` | Créé | Orchestration notifications |
| `toast.service.ts` | Créé | Service de notifications visuelles |
| `toast-container.component.ts` | Créé | UI toasts |
| `shell.component.ts` | Modifié | Initialisation WebSocket |

---

## 🎬 Prochaines Étapes

1. ✅ Frontend implémenté et testé
2. ⏳ Backend doit:
   - Configurer WebSocket/Stomp
   - Publier les notifications d'exécution sur `/topic/scheduled-orders/{playerId}`
   - Optionnel: Publier les alertes sur `/topic/alerts/{playerId}`
3. 🧪 Tester l'intégration end-to-end

---

**Dernière mise à jour:** 22 novembre 2025
