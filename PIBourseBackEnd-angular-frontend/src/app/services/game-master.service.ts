import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environment/environment';

export interface FictiveOrderPayload {
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  priceType: 'MARKET' | 'LIMIT';
  limitPrice?: number;
  description?: string;
}

export interface MarketEventPayload {
  eventType: string;
  symbol?: string | null;
  intensity: number;
}

export interface MarketStats {
  totalOrders: number;
  totalVolume: number;
  activePlayers: number;
  mostTradedSymbol: string;
}

@Injectable({
  providedIn: 'root'
})
export class GameMasterService {
  private baseUrl = `${environment.apiUrl}/game-master`;

  constructor(private http: HttpClient) {}

  /**
   * Injecter un ordre fictif massif dans le carnet d'ordres
   */
  injectFictiveOrder(payload: FictiveOrderPayload): Observable<any> {
    return this.http.post(`${this.baseUrl}/inject-order`, payload);
  }

  /**
   * Déclencher un événement de marché (crash, bull run, etc.)
   */
  triggerMarketEvent(payload: MarketEventPayload): Observable<any> {
    return this.http.post(`${this.baseUrl}/trigger-event`, payload);
  }

  /**
   * Annuler tous les ordres d'un symbole spécifique
   */
  cancelAllOrders(symbol: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/orders/${symbol}`);
  }

  /**
   * Obtenir les statistiques globales du marché
   */
  getMarketStats(): Observable<MarketStats> {
    return this.http.get<MarketStats>(`${this.baseUrl}/stats`);
  }

  /**
   * Obtenir tous les ordres actifs (vue globale)
   */
  getAllOrders(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/allorders`);
  }

  /**
   * Obtenir la liste de tous les joueurs avec leurs statistiques
   */
  getAllPlayers(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/players`);
  }

  /**
   * Obtenir la liste des joueurs actifs (connectés récemment)
   */
  getActivePlayers(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/active-players`);
  }

  /**
   * Forcer l'exécution du matching engine pour un symbole
   */
  forceMatching(symbol: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/force-matching/${symbol}`, {});
  }

  /**
   * Réinitialiser le marché (supprimer tous les ordres)
   */
  resetMarket(): Observable<any> {
    return this.http.post(`${this.baseUrl}/reset`, {});
  }
}
