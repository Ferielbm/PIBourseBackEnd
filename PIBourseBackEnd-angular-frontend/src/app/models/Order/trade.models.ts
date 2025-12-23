export interface Trade {
  id: number;
  buyOrderId: number;
  sellOrderId: number;
  symbol: string;
  price: number;
  quantity: number;
  timestamp: string;
}
