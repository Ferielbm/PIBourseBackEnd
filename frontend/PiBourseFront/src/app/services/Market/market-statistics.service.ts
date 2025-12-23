import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StockPerformance {
  symbol: string;
  totalReturn: string;
  volatility: string;
  startPrice: number;
  endPrice: number;
  priceChange: number;
  totalDays: number;
  averageVolume: number;
  monthlyPerformance: { [key: string]: number };
}

export interface MarketOverview {
  totalStocks: number;
  totalMarketCap: number;
  stocksBySector: { [key: string]: number };
  marketCapBySector: { [key: string]: number };
  averageStockPrice: number;
  lastUpdate: string;
}

@Injectable({
  providedIn: 'root'
})
export class MarketStatisticsService {
  private baseUrl = 'http://localhost:8080/api/market';

  constructor(private http: HttpClient) {}

  /**
   * Obtenir les performances d'une action
   */
  getStockPerformance(symbol: string): Observable<StockPerformance> {
    return this.http.get<StockPerformance>(`${this.baseUrl}/stocks/${symbol}/performance`);
  }

  /**
   * Obtenir les meilleures performances
   */
  getTopPerformers(limit: number = 10): Observable<StockPerformance[]> {
    return this.http.get<StockPerformance[]>(`${this.baseUrl}/stocks/top-performers?limit=${limit}`);
  }

  /**
   * Obtenir l'aperçu général du marché
   */
  getMarketOverview(): Observable<MarketOverview> {
    return this.http.get<MarketOverview>(`${this.baseUrl}/overview`);
  }
}
