import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TimeTravelSession {
  id: number;
  sessionId: string;
  playerId: string;
  rewindToDate: string;
  currentSimulationDate: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED';
  originalPortfolioValue: number;
  alternativePortfolioValue: number;
}

export interface AlternativeTrade {
  symbol: string;
  action: 'BUY' | 'SELL' | 'HOLD';
  quantity: number;
  executionPrice?: number;
  executionDate: string;
  alternativeDecision: string;
}

export interface TimeTravelResult {
  sessionId: string;
  playerId: string;
  rewindPoint: string;
  simulationEndDate: string;
  originalPerformance: number;
  alternativePerformance: number;
  performanceGap: number;
  tradeComparisons: TradeComparison[];
  learningInsights: LearningInsight[];
  riskAssessment: string;
  originalSharpeRatio: number;
  alternativeSharpeRatio: number;
  maxDrawdownImprovement: number;
}

export interface TradeComparison {
  symbol: string;
  originalAction: string;
  alternativeAction: string;
  originalPnL: number;
  alternativePnL: number;
  improvement: number;
}

export interface LearningInsight {
  insightType: string;
  description: string;
  impactScore: number;
  recommendation: string;
}

@Injectable({
  providedIn: 'root'
})
export class TimeTravelService {
  private baseUrl = 'http://localhost:8080/api/time-travel';

  constructor(private http: HttpClient) {}

  /**
   * Démarrer une session de voyage dans le temps
   */
  startSession(playerId: string, rewindToDate: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/start`, null, {
      params: { playerId, rewindToDate }
    });
  }

  /**
   * Exécuter un trade alternatif
   */
  executeAlternativeTrade(sessionId: string, trade: AlternativeTrade): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${sessionId}/trade`, trade);
  }

  /**
   * Terminer et analyser la session
   */
  completeSession(sessionId: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${sessionId}/complete`, {});
  }

  /**
   * Prévisualiser les résultats
   */
  previewResults(sessionId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${sessionId}/preview`);
  }

  /**
   * Obtenir toutes les sessions d'un joueur
   */
  getPlayerSessions(playerId: string): Observable<TimeTravelSession[]> {
    return this.http.get<TimeTravelSession[]>(`${this.baseUrl}/player/${playerId}/sessions`);
  }
}
