// src/app/features/orders/services/trade.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Trade } from '../../../models/Order/models/trade.models';
import { environment } from '../../../environment/environment';

@Injectable({
  providedIn: 'root'
})
export class TradeService {

  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET /players/{playerId}/trades?symbol=...
  getTradesByPlayer(
    playerId: number,
    symbol?: string
  ): Observable<Trade[]> {
    let params = new HttpParams();
    if (symbol) {
      params = params.set('symbol', symbol);
    }

    return this.http.get<Trade[]>(
      `${this.baseUrl}/players/${playerId}/trades`,
      { params }
    );
  }
}
