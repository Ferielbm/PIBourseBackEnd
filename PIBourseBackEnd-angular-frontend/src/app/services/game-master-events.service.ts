import { Injectable } from '@angular/core';
import { WebsocketService } from './websocket.service';
import { ToastService } from './toast.service';
import { NotificationHistoryService } from './notification-history.service';

export interface GameMasterEvent {
  type: 'GAME_MASTER_ACTION';
  actionType: string;
  symbol: string;
  side: string;
  quantity: number;
  message: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class GameMasterEventsService {
  private unsubscribeFn?: () => void;

  constructor(
    private websocketService: WebsocketService,
    private toastService: ToastService,
    private notificationHistoryService: NotificationHistoryService
  ) {}

  /**
   * Démarrer l'écoute des événements Game Master
   */
  startListening(playerId: number): void {
    this.websocketService.connect().then(() => {
      console.log('🎮 Écoute des événements Game Master démarrée pour joueur', playerId);

      // Écouter sur le topic personnel du joueur
      this.unsubscribeFn = this.websocketService.subscribe(
        `/topic/game-master-events/${playerId}`,
        (event: GameMasterEvent) => {
          this.handleGameMasterEvent(event);
        }
      );
    }).catch(err => {
      console.error('❌ Erreur connexion WebSocket:', err);
    });
  }

  /**
   * Arrêter l'écoute
   */
  stopListening(): void {
    if (this.unsubscribeFn) {
      this.unsubscribeFn();
      this.unsubscribeFn = undefined;
    }
  }

  /**
   * Traiter un événement du Game Master
   */
  private handleGameMasterEvent(event: GameMasterEvent): void {
    console.log('🎮 Événement Game Master reçu:', event);

    // Déterminer l'icône et la couleur selon le type d'action
    let icon = '🎮';
    let toastType: 'success' | 'info' | 'warning' | 'error' = 'info';

    switch (event.actionType) {
      case 'MASSIVE_ORDER':
        icon = event.side === 'BUY' ? '📈' : '📉';
        toastType = event.side === 'BUY' ? 'success' : 'warning';
        break;
      case 'CRASH':
        icon = '📉';
        toastType = 'error';
        break;
      case 'BULL_RUN':
        icon = '📈';
        toastType = 'success';
        break;
      case 'VOLATILITY':
        icon = '⚡';
        toastType = 'warning';
        break;
      case 'MANIPULATION':
        icon = '🎯';
        toastType = 'warning';
        break;
    }

    // Afficher une notification toast
    const message = `${icon} ${event.message}`;
    
    switch (toastType) {
      case 'success':
        this.toastService.success(message, 10000);
        break;
      case 'warning':
        this.toastService.warning(message, 10000);
        break;
      case 'error':
        this.toastService.error(message, 10000);
        break;
      default:
        this.toastService.info(message, 10000);
    }

    // Ajouter à l'historique
    this.notificationHistoryService.addNotification(
      'ALERT_TRIGGERED',
      'Action du Meneur de Jeu',
      event.message,
      {
        actionType: event.actionType,
        symbol: event.symbol,
        side: event.side,
        quantity: event.quantity,
        timestamp: event.timestamp
      }
    );

    // Jouer un son (optionnel)
    this.playNotificationSound();
  }

  /**
   * Jouer un son de notification
   */
  private playNotificationSound(): void {
    try {
      const audio = new Audio('assets/notification-sound.mp3');
      audio.volume = 0.3;
      audio.play().catch(err => {
        // Ignorer si le navigateur bloque l'autoplay
        console.log('Son de notification bloqué:', err);
      });
    } catch (err) {
      console.log('Erreur lecture son:', err);
    }
  }
}
