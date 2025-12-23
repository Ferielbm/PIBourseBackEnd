// src/app/features/services/scheduled-order.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ScheduledOrderRequest {
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  minPrice?: number;
  maxPrice?: number;
  notifyOnly: boolean;
  notifyWhenApproachMin: boolean;
  approachThresholdPct?: number;
  approachCooldownMinutes?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ScheduledOrderService {
  private readonly baseUrl = 'http://localhost:8084/api/players';

  constructor(private http: HttpClient) {}

  createScheduledOrder(playerId: number, body: ScheduledOrderRequest): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${playerId}/scheduled-orders`, body);
  }

  getScheduledOrders(playerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${playerId}/scheduled-orders`);
  }

  getScheduledOrderById(playerId: number, orderId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${playerId}/scheduled-orders/${orderId}`);
  }

  deleteScheduledOrder(playerId: number, orderId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${playerId}/scheduled-orders/${orderId}`);
  }

  reactivateScheduledOrder(playerId: number, orderId: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${playerId}/scheduled-orders/${orderId}/reactivate`, {});
  }
}
