export interface Stock {
  id: number;
  symbol: string;
  name: string;
  lastPrice: number;
  variation?: number;
  marketCap?: number;
  volume?: number;
  createdAt?: string;
  updatedAt?: string;
}
