// src/app/features/orders/services/order.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';

import { Order, PlaceOrderRequest, BookSnapshot } from '../../models/Order/order.models';
import { environment } from '../../environment/environment';

// Simple stock interface for mapping
interface StockMap {
  id: number;
  symbol: string;
  name?: string;
}

// Mock stocks en cas d'erreur API
const MOCK_STOCKS: StockMap[] = [
  { id: 1, symbol: 'AAPL', name: 'Apple Inc' },
  { id: 2, symbol: 'MSFT', name: 'Microsoft' },
  { id: 3, symbol: 'TSLA', name: 'Tesla' },
  { id: 4, symbol: 'GOOG', name: 'Google' },
  { id: 5, symbol: 'AMZN', name: 'Amazon' }
];

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly baseUrl = environment.apiUrl; // ex: http://localhost:8084/api
  private stockCache: Map<number, string> = new Map();
  // Cache temporaire pour symboles des ordres créés localement
  private createdOrdersCache: Map<number, string> = new Map();
  private readonly CREATED_ORDERS_KEY = 'pib_created_orders_cache_v1';

  constructor(private http: HttpClient) {
    // charger le cache persistant si présent
    this.loadCreatedOrdersCacheFromStorage();
  }

  // POST /players/{playerId}/orders
  placeOrder(
    playerId: number,
    payload: PlaceOrderRequest
  ): Observable<Order> {
    return this.http.post<Order>(
      `${this.baseUrl}/players/${playerId}/orders`,
      payload
    ).pipe(
      tap((order: Order) => {
        // Si le backend ne retourne pas le symbole, garder en cache le symbole envoyé
        try {
          if (order && order.id) {
            // stocker le symbole envoyé côté client pour restaurer l'affichage
            this.createdOrdersCache.set(order.id, payload.symbol);
            // persister
            this.saveCreatedOrdersCacheToStorage();
          }
        } catch (e) {
          // nothing
        }
      })
    );
  }

  // GET /players/{playerId}/orders
  getOrdersByPlayer(playerId: number): Observable<Order[]> {
    return this.http.get<Order[]>(
      `${this.baseUrl}/players/${playerId}/orders`
    ).pipe(
      map(orders => {
        // Essayer de charger les symboles manquants depuis le stockId
        return orders.map(order => {
          // Priorité: symbole retourné par l'API
          if (order.symbol && order.symbol.trim()) {
            return order;
          }

          // Ensuite: symbole depuis cache créé au moment de la création
          if (order.id && this.createdOrdersCache.has(order.id)) {
            order.symbol = this.createdOrdersCache.get(order.id) || 'N/A';
            return order;
          }

          // Ensuite: symbole depuis stockId
          if (!order.symbol && order.stockId && this.stockCache.has(order.stockId)) {
            order.symbol = this.stockCache.get(order.stockId) || 'N/A';
          }

          return order;
        });
      }),
      catchError(err => {
        console.error('Erreur chargement ordres:', err);
        return of([]);
      })
    );
  }

  // GET /allorders (tous les players)
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(
      `${this.baseUrl}/allorders`
    ).pipe(
      map(orders => {
        return orders.map(order => {
          if (order.symbol && order.symbol.trim()) {
            return order;
          }

          if (order.id && this.createdOrdersCache.has(order.id)) {
            order.symbol = this.createdOrdersCache.get(order.id) || 'N/A';
            return order;
          }

          if (!order.symbol && order.stockId && this.stockCache.has(order.stockId)) {
            order.symbol = this.stockCache.get(order.stockId) || 'N/A';
          }

          return order;
        });
      }),
      catchError(err => {
        console.error('Erreur chargement ordres (tous les players):', err);
        return of([]);
      })
    );
  }

  // GET /players/{playerId}/orders/{orderId}
  getOrderById(playerId: number, orderId: number): Observable<Order> {
    return this.http.get<Order>(
      `${this.baseUrl}/players/${playerId}/orders/${orderId}`
    );
  }

  // POST /players/{playerId}/orders/{orderId}/cancel
  cancelOrder(playerId: number, orderId: number): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/players/${playerId}/orders/${orderId}/cancel`,
      {}
    );
  }

  // GET /players/{playerId}/orderbook/{symbol}
  getOrderBook(playerId: number, symbol: string): Observable<BookSnapshot> {
    return this.http.get<BookSnapshot>(
      `${this.baseUrl}/players/${playerId}/orderbook/${symbol}`
    );
  }

  /**
   * Charger et mettre en cache les stocks avec leurs symboles depuis /stocks
   */
  cacheStocks(): Observable<void> {
    // Appeler l'endpoint /stocks
    return this.http.get<any[]>(`${this.baseUrl}/stocks`).pipe(
      map(apiStocks => {
        if (apiStocks && apiStocks.length > 0) {
          // Backend retourne un tableau de stocks avec symbol
          apiStocks.forEach((stock, index) => {
            this.stockCache.set(index + 1, stock.symbol);
          });
          console.log('✅ Cache des stocks chargé depuis /stocks:', apiStocks.length, 'stocks');
        }
      }),
      catchError(err => {
        console.warn('Erreur chargement /stocks, utilisation des données par défaut:', err);
        // Utiliser les données par défaut en cas d'erreur
        MOCK_STOCKS.forEach(stock => {
          this.stockCache.set(stock.id, stock.symbol);
        });
        console.log('📦 Cache des stocks initialisé avec données par défaut:', MOCK_STOCKS.length, 'stocks');
        return of(undefined);
      })
    );
  }

  /**
   * Retourner le symbole depuis le cache basé sur stockId
   */
  getSymbolFromCache(stockId: number): string | null {
    return this.stockCache.get(stockId) || null;
  }

  /** Persist createdOrdersCache to localStorage */
  private saveCreatedOrdersCacheToStorage(): void {
    try {
      const obj: { [id: number]: string } = {};
      this.createdOrdersCache.forEach((v, k) => (obj[k] = v));
      localStorage.setItem(this.CREATED_ORDERS_KEY, JSON.stringify(obj));
    } catch (e) {
      // ignore
    }
  }

  /** Load createdOrdersCache from localStorage */
  private loadCreatedOrdersCacheFromStorage(): void {
    try {
      const raw = localStorage.getItem(this.CREATED_ORDERS_KEY);
      if (!raw) return;
      const obj = JSON.parse(raw) as { [id: string]: string };
      Object.keys(obj).forEach(k => {
        const id = Number(k);
        if (!isNaN(id)) {
          this.createdOrdersCache.set(id, obj[k]);
        }
      });
    } catch (e) {
      // ignore
    }
  }
}
