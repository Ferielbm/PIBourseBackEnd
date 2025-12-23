import { OrderSide, OrderType, TimeInForce } from './order.models';

export type ScheduledOrderStatus =
  | 'PENDING'
  | 'TRIGGERED'
  | 'EXECUTED'
  | 'REFUSED'
  | 'CANCELLED'
  | 'FAILED';

export interface ScheduledOrder {
  id: number;
  version: number;
  actor: string;
  playerId: number;
  desiredSymbol: string;
  side: OrderSide;
  quantity: number;
  minPrice?: number | null;
  maxPrice?: number | null;
  notifyOnly: boolean;
  notifyWhenApproachMin: boolean;
  approachThresholdPct?: number | null;
  approachCooldownMinutes: number;
  lastApproachNotifiedAt?: string | null;
  status: ScheduledOrderStatus;
  type: OrderType;
  tif: TimeInForce;
  price?: number | null;
  failureReason?: string | null;
  createdAt: string;
  updatedAt?: string | null;
  triggeredAt?: string | null;
}

export interface CreateScheduledOrderRequest {
  symbol: string;
  side: OrderSide;
  quantity: number;
  minPrice?: number | null;
  maxPrice?: number | null;
  notifyOnly?: boolean;
  notifyWhenApproachMin?: boolean;
  approachThresholdPct?: number | null;
  approachCooldownMinutes?: number | null;
}
