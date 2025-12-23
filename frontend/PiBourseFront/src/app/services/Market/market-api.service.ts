import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { API_BASE_URL } from '../../config/api.config';
import { PriceHistory } from '../../models/Market/price-history.model';

@Injectable({ providedIn: 'root' })
export class MarketApiService {
  private readonly http = inject(HttpClient);

  async getPriceHistory(symbol: string, startDate: string, endDate: string): Promise<PriceHistory[]> {
    const url = `${API_BASE_URL}/api/market/stocks/${encodeURIComponent(symbol)}/history?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`;
    return this.requestPriceHistory(url);
  }

  async getPriceHistoryAll(symbol: string): Promise<PriceHistory[]> {
    const url = `${API_BASE_URL}/api/market/stocks/${encodeURIComponent(symbol)}/history`;
    return this.requestPriceHistory(url);
  }

  private requestPriceHistory(url: string): Promise<PriceHistory[]> {
    return firstValueFrom(
      this.http
        .get<PriceHistory[]>(url, {
          headers: { Accept: 'application/json' }
        })
        .pipe(
          catchError((error) => {
            const status = error.status ?? 'UNKNOWN';
            const statusText = error.statusText ?? 'Error';
            const message = error.error?.message || error.message || '';
            return throwError(() => new Error(`HTTP ${status} ${statusText} - ${message}`));
          })
        )
    );
  }
}
