export interface PriceHistory {
  dateTime: string;
  openPrice: string | number;
  highPrice: string | number;
  lowPrice: string | number;
  closePrice: string | number;
  volume: number;
}
