import { Injectable, OnDestroy } from '@angular/core';
import { Subject, BehaviorSubject } from 'rxjs';
import { WebsocketService } from './websocket.service';
import { MarketAlertService } from './Order/market-alert.service';
import { PriceAlert } from '../models/Order/market-alert.models';

export interface OrderExecutionNotification {
  orderId: number;
  symbol: string;
  side: 'BUY' | 'SELL';
  executedPrice: number;
  executedQuantity: number;
  executedAt: string;
  remainingQuantity: number;
  status: string;
}

export interface AlertTriggeredNotification {
  alertId: number;
  symbol: string;
  alertType: 'PRICE_REACHED' | 'PRICE_APPROACHING';
  minPrice?: number;
  maxPrice?: number;
  currentPrice: number;
  triggeredAt: string;
}

export interface DecisionTicketNotification {
  ticketId: number;
  playerId: number;
  symbol: string;
  side: 'BUY' | 'SELL';
  reason: 'IN_RANGE' | 'APPROACHING_MIN';
  status: string;
  foundPrice: number;
  suggestedQuantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class OrderExecutionNotificationService implements OnDestroy {
  private orderExecutionNotifications = new Subject<OrderExecutionNotification>();
  private alertTriggeredNotifications = new Subject<AlertTriggeredNotification>();
  private decisionTicketNotifications = new Subject<DecisionTicketNotification>();
  private unsubscribeFns: Array<() => void> = [];

  public orderExecution$ = this.orderExecutionNotifications.asObservable();
  public alertTriggered$ = this.alertTriggeredNotifications.asObservable();
  public decisionTicket$ = this.decisionTicketNotifications.asObservable();

  constructor(
    private websocketService: WebsocketService,
    private alertService: MarketAlertService
  ) {}

  /**
   * Initialise l'écoute des notifications WebSocket pour un joueur
   */
  initializeForPlayer(playerId: number): void {
    this.cleanup();

    // 🔔 Écoute des exécutions d'ordres planifiés
    const unsubOrder = this.websocketService.subscribe(
      `/topic/scheduled-orders/${playerId}`,
      (data: OrderExecutionNotification) => {
        console.log('📊 Ordre exécuté reçu:', data);
        this.orderExecutionNotifications.next(data);
        
        // Vérifier si cet ordre correspond à une alerte
        this.checkAndTriggerAlerts(playerId, data);
      }
    );
    this.unsubscribeFns.push(unsubOrder);

    // 🔔 Écoute des alertes déclenchées directement
    const unsubAlert = this.websocketService.subscribe(
      `/topic/alerts/${playerId}`,
      (data: AlertTriggeredNotification) => {
        console.log('🚨 Alerte déclenchée:', data);
        this.alertTriggeredNotifications.next(data);
      }
    );
    this.unsubscribeFns.push(unsubAlert);

    // 🎫 Écoute des tickets de décision (ordres en mode notifyOnly)
    const unsubTicket = this.websocketService.subscribe(
      `/topic/decision-tickets/${playerId}`,
      (data: DecisionTicketNotification) => {
        console.log('🎫 Ticket de décision reçu:', data);
        this.decisionTicketNotifications.next(data);
      }
    );
    this.unsubscribeFns.push(unsubTicket);
  }

  /**
   * Vérifie si une exécution d'ordre correspond aux conditions des alertes
   */
  private checkAndTriggerAlerts(playerId: number, execution: OrderExecutionNotification): void {
    this.alertService.getAlerts(playerId).subscribe({
      next: (alerts: PriceAlert[]) => {
        for (const alert of alerts) {
          // Vérifier si l'alerte concerne le même symbole
          if (alert.symbol.toUpperCase() !== execution.symbol.toUpperCase()) {
            continue;
          }

          // Vérifier si l'alerte est active
          if (alert.status !== 'ACTIVE') {
            continue;
          }

          // Vérifier si le prix exécuté correspond aux plages d'alerte
          const priceMatches = this.isPriceInAlertRange(
            execution.executedPrice,
            alert.minPrice,
            alert.maxPrice
          );

          if (priceMatches) {
            const notification: AlertTriggeredNotification = {
              alertId: alert.id || 0,
              symbol: execution.symbol,
              alertType: 'PRICE_REACHED',
              minPrice: alert.minPrice || undefined,
              maxPrice: alert.maxPrice || undefined,
              currentPrice: execution.executedPrice,
              triggeredAt: execution.executedAt
            };

            console.log('🔔 Alerte déclenchée (prix atteint):', notification);
            this.alertTriggeredNotifications.next(notification);
            
            // Optionnel: Afficher une notification système
            this.showNotification(notification);
          }
        }
      },
      error: (err) => {
        console.error('Erreur vérification alertes:', err);
      }
    });
  }

  /**
   * Vérifie si un prix se situe dans la plage d'une alerte
   */
  private isPriceInAlertRange(
    price: number,
    minPrice: number | null | undefined,
    maxPrice: number | null | undefined
  ): boolean {
    if (minPrice != null && price < minPrice) {
      return false;
    }
    if (maxPrice != null && price > maxPrice) {
      return false;
    }
    return true;
  }

  /**
   * Affiche une notification système (toast/popup)
   */
  private showNotification(notification: AlertTriggeredNotification): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(`Alerte Prix - ${notification.symbol}`, {
        body: `Prix atteint: ${notification.currentPrice}€`,
        icon: '/assets/icon.png',
        tag: `alert-${notification.alertId}`,
        requireInteraction: true
      });
    }
  }

  /**
   * Demande la permission pour les notifications du navigateur
   */
  requestNotificationPermission(): void {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission((permission) => {
        if (permission === 'granted') {
          console.log('✅ Notifications autorisées');
        }
      });
    }
  }

  /**
   * Nettoie les souscriptions
   */
  private cleanup(): void {
    for (const unsubscribe of this.unsubscribeFns) {
      unsubscribe();
    }
    this.unsubscribeFns = [];
  }

  ngOnDestroy(): void {
    this.cleanup();
  }
}
