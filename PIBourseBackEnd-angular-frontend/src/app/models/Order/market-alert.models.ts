export interface PriceAlert {
  id?: number;
  playerId?: number;
  symbol: string;
  minPrice?: number | null;
  maxPrice?: number | null;
  status?: 'ACTIVE' | 'PAUSED' | 'CANCELLED';
  createdAt?: string;
  updatedAt?: string;
}