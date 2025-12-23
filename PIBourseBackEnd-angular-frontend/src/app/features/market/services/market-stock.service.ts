import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environment/environment';
import { Stock } from '../models/stock.models';

@Injectable({
  providedIn: 'root'
})
export class MarketStockService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Retrieve all stocks with market data and prices from real API
   */
  getAllStocks(): Observable<Stock[]> {
    // Fetch from real backend /market/stocks endpoint
    return this.http.get<any[]>(`${this.baseUrl}/market/stocks`).pipe(
      map(apiStocks => {
        // Transform backend response to Stock interface
        // API returns: {symbol, companyName, sector, marketCap, currentPrice, priceHistoryList, orderBook, ...}
        // We need: {id, symbol, name, lastPrice, variation?, marketCap?, volume?}
        return apiStocks.map((stock, index) => ({
          id: index + 1,
          symbol: stock.symbol,
          name: stock.companyName || stock.symbol,
          lastPrice: stock.currentPrice || 0,
          variation: 0,
          marketCap: stock.marketCap,
          volume: stock.orderBook?.totalBidVolume || 0
        }));
      }),
      catchError(err => {
        console.error('Erreur chargement stocks depuis /market/stocks:', err);
        // Fallback to mock data if API fails
        return of(this.getMockStocks());
      })
    );
  }

  /**
   * Mock stocks as fallback
   */
  private getMockStocks(): Stock[] {
    const mockStocks: Stock[] = [
      {
        id: 1,
        symbol: 'AAPL',
        name: 'Apple Inc',
        lastPrice: 182.63,
        variation: 2.5,
        marketCap: 2800000000000,
        volume: 50000000
      },
      {
        id: 2,
        symbol: 'MSFT',
        name: 'Microsoft',
        lastPrice: 374.51,
        variation: 1.8,
        marketCap: 2800000000000,
        volume: 30000000
      },
      {
        id: 3,
        symbol: 'TSLA',
        name: 'Tesla',
        lastPrice: 248.42,
        variation: -1.2,
        marketCap: 800000000000,
        volume: 120000000
      },
      {
        id: 4,
        symbol: 'GOOGL',
        name: 'Alphabet Inc.',
        lastPrice: 138.57,
        variation: 0.5,
        marketCap: 1750000000000,
        volume: 25000000
      },
      {
        id: 5,
        symbol: 'AMZN',
        name: 'Amazon.com Inc.',
        lastPrice: 151.94,
        variation: 3.2,
        marketCap: 1550000000000,
        volume: 45000000
      }
    ];
    return mockStocks;
  }

  /**
   * Retrieve a specific stock by symbol
   */
  getStockBySymbol(symbol: string): Observable<Stock> {
    return this.getAllStocks().pipe(
      map(stocks => {
        const stock = stocks.find(s => s.symbol === symbol);
        return stock || { id: 0, symbol, name: symbol, lastPrice: 0 };
      })
    );
  }
}
