import { Injectable } from '@angular/core';
import { WebsocketService } from './websocket.service';
import { NotificationHistoryService } from './notification-history.service';
import { ToastService } from './toast.service';

export interface AlertTriggeredPayload {
  alertId: number;
  symbol: string;
  currentPrice: number;
  minPrice?: number;
  maxPrice?: number;
  triggeredType: 'MIN_REACHED' | 'MAX_REACHED' | 'IN_RANGE';
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class MarketAlertNotificationService {
  private unsubscribeFn: (() => void) | null = null;

  constructor(
    private websocketService: WebsocketService,
    private historyService: NotificationHistoryService,
    private toastService: ToastService
  ) {}

  /**
   * Initialise l'écoute des alertes déclenchées via WebSocket
   */
  init(playerId: number): void {
    if (this.unsubscribeFn) {
      console.warn('⚠️ MarketAlertNotificationService déjà initialisé');
      return;
    }

    const topic = `/topic/player/${playerId}/alerts`;
    console.log('🎯 MarketAlertNotificationService.init() appelé pour joueur:', playerId);
    console.log('🔔 Écoute des alertes sur:', topic);
    console.log('📡 État WebSocket:', this.websocketService);

    this.unsubscribeFn = this.websocketService.subscribe(
      topic,
      (payload: AlertTriggeredPayload) => {
        console.log('🚨🚨🚨 ALERTE DÉCLENCHÉE REÇUE 🚨🚨🚨');
        console.log('Payload complet:', JSON.stringify(payload, null, 2));
        this.handleAlertTriggered(payload);
      }
    );

    console.log('✅ Souscription aux alertes terminée');
  }

  /**
   * Traite une alerte déclenchée
   */
  private handleAlertTriggered(payload: AlertTriggeredPayload): void {
    console.log('📥 handleAlertTriggered() appelé avec:', payload);
    
    const { symbol, currentPrice, triggeredType, minPrice, maxPrice } = payload;

    // Construire le message
    let message = '';
    if (triggeredType === 'IN_RANGE' && minPrice != null && maxPrice != null) {
      message = `Le prix de ${symbol} (${currentPrice}€) est dans l'intervalle [${minPrice}€ - ${maxPrice}€]`;
    } else if (triggeredType === 'MIN_REACHED' && minPrice != null) {
      message = `Le prix de ${symbol} (${currentPrice}€) a atteint ou dépassé le minimum (${minPrice}€)`;
    } else if (triggeredType === 'MAX_REACHED' && maxPrice != null) {
      message = `Le prix de ${symbol} (${currentPrice}€) a atteint ou est sous le maximum (${maxPrice}€)`;
    } else {
      message = `Alerte déclenchée pour ${symbol} à ${currentPrice}€`;
    }

    console.log('💬 Message construit:', message);

    // Ajouter à l'historique
    console.log('💾 Ajout à l\'historique...');
    this.historyService.addNotification(
      'ALERT_TRIGGERED',
      `🚨 Alerte: ${symbol}`,
      message,
      {
        alertId: payload.alertId,
        symbol: payload.symbol,
        currentPrice: payload.currentPrice,
        triggeredType: payload.triggeredType
      }
    );

    // Afficher un toast cliquable
    console.log('🍞 Affichage du toast...');
    this.toastService.warning(
      `🚨 Alerte déclenchée: ${symbol} - ${message}`,
      8000,
      true,
      { symbol: symbol }
    );
    
    console.log('✅ Traitement de l\'alerte terminé');
  }

  /**
   * Arrête l'écoute des notifications
   */
  destroy(): void {
    if (this.unsubscribeFn) {
      this.unsubscribeFn();
      this.unsubscribeFn = null;
      console.log('🔕 Écoute des alertes arrêtée');
    }
  }
}
