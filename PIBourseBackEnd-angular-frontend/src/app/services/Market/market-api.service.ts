import { Injectable, inject } from '@angular/core';
import { API_BASE_URL } from '../../config/api.config';
import { PriceHistory } from '../../models/Market/price-history.model';

@Injectable({ providedIn: 'root' })
export class MarketApiService {
  async getPriceHistory(symbol: string, startDate: string, endDate: string): Promise<PriceHistory[]> {
    const url = `${API_BASE_URL}/api/market/stocks/${encodeURIComponent(symbol)}/history?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`;
    const res = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(`HTTP ${res.status} ${res.statusText} - ${text}`);
    }
    return res.json();
  }

  async getPriceHistoryAll(symbol: string): Promise<PriceHistory[]> {
    const url = `${API_BASE_URL}/api/market/stocks/${encodeURIComponent(symbol)}/history`;
    const res = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(`HTTP ${res.status} ${res.statusText} - ${text}`);
    }
    return res.json();
  }
}
