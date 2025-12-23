import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// ✅ On réutilise le même modèle que dans le module Market
import { Stock } from '../../features/market/models/stock.models';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private readonly baseUrl = 'http://localhost:8084/api';

  constructor(private http: HttpClient) {}

  /**
   * Récupère tous les stocks depuis le backend.
   * Exemple endpoint : GET /api/stocks
   */
  getAllStocks(): Observable<Stock[]> {
    return this.http.get<Stock[]>(`${this.baseUrl}/stocks`);
  }

  /**
   * Alias utilisé par d'autres composants (scheduled-order-form).
   */
  getAllSymbols(): Observable<Stock[]> {
    return this.getAllStocks();
  }
}
