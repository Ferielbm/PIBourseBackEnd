import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DecisionTicket {
  id: number;
  playerId: number;
  symbol: string;
  side: 'BUY' | 'SELL';
  reason: 'IN_RANGE' | 'APPROACHING_MIN';
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';
  foundPrice: number;
  suggestedQuantity: number;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DecisionTicketService {
  private baseUrl = 'http://localhost:8084/api/players';

  constructor(private http: HttpClient) {}

  /**
   * Récupère tous les tickets de décision en attente pour un joueur
   */
  getPendingTickets(playerId: number): Observable<DecisionTicket[]> {
    return this.http.get<DecisionTicket[]>(`${this.baseUrl}/${playerId}/scheduled-orders/tickets/pending`);
  }

  /**
   * Accepte un ticket de décision et passe l'ordre
   */
  acceptTicket(playerId: number, ticketId: number, quantity?: number): Observable<DecisionTicket> {
    const body = quantity ? { accept: true, quantity } : { accept: true };
    return this.http.post<DecisionTicket>(`${this.baseUrl}/${playerId}/scheduled-orders/tickets/${ticketId}/decide`, body);
  }

  /**
   * Rejette un ticket de décision
   */
  rejectTicket(playerId: number, ticketId: number): Observable<DecisionTicket> {
    return this.http.post<DecisionTicket>(`${this.baseUrl}/${playerId}/scheduled-orders/tickets/${ticketId}/decide`, { accept: false });
  }
}
