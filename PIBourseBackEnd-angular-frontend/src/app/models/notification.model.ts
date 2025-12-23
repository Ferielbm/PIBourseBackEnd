export interface Notification {
  id: number;
  type: 'ORDER_EXECUTED' | 'DECISION_TICKET' | 'ALERT_TRIGGERED' | 'PRICE_CHANGE' | 'MARKET_EVENT' | 'GAME_MASTER_EVENT';
  title: string;
  message: string;
  read: boolean;
  createdAt: Date;
  data?: any; // Données additionnelles (ticketId, orderId, etc.)
}
