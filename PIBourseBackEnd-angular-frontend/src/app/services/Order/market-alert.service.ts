import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PriceAlert } from '../../models/Order/market-alert.models';

@Injectable({
  providedIn: 'root'
})
export class MarketAlertService {
  private readonly baseUrl = 'http://localhost:8084/api';

  constructor(private http: HttpClient) {}

  // --- CRÉER UNE ALERTE ---
  createAlert(playerId: number, alert: PriceAlert): Observable<PriceAlert> {
    return this.http.post<PriceAlert>(
      `${this.baseUrl}/players/${playerId}/alerts`,
      {
        symbol: alert.symbol,
        minPrice: alert.minPrice ?? null,
        maxPrice: alert.maxPrice ?? null
      }
    );
  }

  // --- LISTER LES ALERTES ---
  getAlerts(playerId: number): Observable<PriceAlert[]> {
    return this.http.get<PriceAlert[]>(
      `${this.baseUrl}/players/${playerId}/alerts`
    );
  }

  // --- CHANGER LE STATUT ---
  updateStatus(
    playerId: number,
    alertId: number,
    status: 'ACTIVE' | 'PAUSED' | 'CANCELLED'
  ): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/players/${playerId}/alerts/${alertId}/status`,
      { status }
    );
  }

  // --- SUPPRIMER ---
  deleteAlert(playerId: number, alertId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/players/${playerId}/alerts/${alertId}`
    );
  }
}
