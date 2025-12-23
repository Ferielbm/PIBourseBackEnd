// ===== ENUMS =====
export type OrderSide = 'BUY' | 'SELL';
export type OrderType = 'MARKET' | 'LIMIT';
export type TimeInForce = 'GTC' | 'IOC' | 'FOK';
export type OrderStatus =
  | 'PENDING'
  | 'OPEN'
  | 'PARTIALLY_FILLED'
  | 'FILLED'
  | 'CANCELED'
  | 'CANCELLED'
  | 'REJECTED';

// ====== ORDER ======
export interface Order {
  id: number;
  playerId: number;
  stockId?: number;
  symbol: string;
  side: OrderSide;
  type: OrderType;
  tif: TimeInForce;
  status: OrderStatus;
  quantity: number;
  remainingQuantity: number;
  price?: number | null;
  executedPrice?: number | null;
  createdAt: string;
  updatedAt?: string | null;
  version?: number;
}

// ====== PLACE ORDER DTO ======
export interface PlaceOrderRequest {
  symbol: string;
  side: OrderSide;
  type: OrderType;
  tif: TimeInForce;
  quantity: number;
  price?: number | null;
}

// ====== CANCEL ORDER DTO ======
export interface CancelOrderRequest {
  orderId: number;
}

// ====== BOOK SNAPSHOT ======
export interface BookSnapshot {
  symbol: string;
  bids: { [price: string]: number };
  asks: { [price: string]: number };
  lastPrice?: number | null;
  timestamp?: string | null;
}
