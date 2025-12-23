export type WalletReservationStatus = 'ACTIVE' | 'CONSUMED' | 'RELEASED';

export interface WalletReservation {
  id: number;
  version: number;
  playerId: number;
  orderId: number;
  amountReserved: number;
  remainingAmount: number;
  status: WalletReservationStatus;
  reason?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}
