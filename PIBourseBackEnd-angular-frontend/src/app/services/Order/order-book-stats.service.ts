import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer, switchMap } from 'rxjs';
import { environment } from '../../environment/environment';

export interface OrderBookStats {
  symbol: string;
  bestBid: number | null;
  bestAsk: number | null;
  lastPrice: number | null;
}

@Injectable({ providedIn: 'root' })
export class OrderBookStatsService {
  private baseUrl = environment.apiUrl; // ensure apiUrl is defined

  constructor(private http: HttpClient) {}

  getStats(symbol: string): Observable<OrderBookStats> {
    // Avoid double '/api' if environment.apiUrl already includes it
    const url = `${this.baseUrl}/orderbook/${symbol}/stats`;
    return this.http.get<OrderBookStats>(url);
  }

  pollStats(symbol: string, intervalMs = 2000): Observable<OrderBookStats> {
    return timer(0, intervalMs).pipe(
      switchMap(() => this.getStats(symbol))
    );
  }
}
