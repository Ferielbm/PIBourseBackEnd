import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject, combineLatest } from 'rxjs';
import { map, switchMap, tap } from 'rxjs/operators';
import { PriceHistory } from '../../../../models/Market/price-history.model';
import { MarketApiService } from '../../../../services/Market/market-api.service';
import { Timeframe, getTimeframeDuration } from '../models/timeframe.model';
import { SimulationTimeService } from '../../../../services/Market/simulation-time.service';

export interface CandleSeriesPoint {
  time: number;
  open: number;
  high: number;
  low: number;
  close: number;
}

export interface LineSeriesPoint {
  time: number;
  value: number;
}

export interface VolumeSeriesPoint {
  time: number;
  value: number;
  color?: string;
}

export interface ChartSeriesPayload {
  candles: CandleSeriesPoint[];
  line: LineSeriesPoint[];
  volumes: VolumeSeriesPoint[];
}

@Injectable({ providedIn: 'root' })
export class ChartDataService {
  private readonly historicalContextStart = new Date('2022-10-01T00:00:00Z');
  private readonly historyCache = new Map<string, PriceHistory[]>();
  private realTimeUpdates = new Subject<PriceHistory>();
  private currentSymbol = new BehaviorSubject<string>('');
  private currentTimeframe = new BehaviorSubject<Timeframe>('D1');
  private historicalDataLoaded = false;

  constructor(
    private readonly marketApi: MarketApiService,
    private simulationTime: SimulationTimeService
  ) {
    // S'abonner aux mises à jour de temps pour gérer le rafraîchissement des données
    this.simulationTime.currentSimDateTime$.subscribe(() => {
      // Implémenter la logique de mise à jour en temps réel
    });
  }

  /**
   * Charge les données historiques et prépare les séries pour le graphique
   * @param symbol Symbole boursier
   * @param timeframe Période temporelle (ex: 'D1' pour journalier)
   * @param rangeEnd Date de fin pour le chargement des données
   * @param bars Nombre maximum de bougies à retourner
   * @param includeHistorical Inclure les données historiques avant la date de début de la simulation
   */
  async loadSeries(
    symbol: string,
    timeframe: Timeframe,
    rangeEnd: Date,
    bars = 800, // Augmenté pour plus de données historiques
    includeHistorical = true
  ): Promise<ChartSeriesPayload> {
    this.currentSymbol.next(symbol);
    this.currentTimeframe.next(timeframe);

    // Charger les données historiques si nécessaire
    let history = this.historyCache.get(symbol) || [];
    
    if (!this.historicalDataLoaded || history.length === 0) {
      history = await this.loadHistory(symbol);
      this.historyCache.set(symbol, history);
      this.historicalDataLoaded = true;
    }
    
    const sanitized = this.ensureSorted(history);
    
    // Filtrer les données pour inclure l'historique depuis octobre 2022
    let filtered = sanitized;
    if (rangeEnd) {
      // Inclure les données depuis octobre 2022 jusqu'à la date de simulation
      const historicalStart = new Date('2022-10-01T00:00:00Z').getTime();
      
      filtered = sanitized.filter(record => {
        const recordTime = new Date(record.dateTime).getTime();
        return recordTime <= rangeEnd.getTime() && recordTime >= historicalStart;
      });
    }
    
    const aggregated = this.aggregateByTimeframe(filtered, timeframe);

    // Créer les séries de données pour le graphique avec des bougies plus fines
    const candles: CandleSeriesPoint[] = [];
    const line: LineSeriesPoint[] = [];
    const volumes: VolumeSeriesPoint[] = [];

    // Utiliser toutes les données disponibles pour une meilleure résolution
    const sliced = bars > 0 ? aggregated.slice(-bars) : aggregated;

    for (const row of sliced) {
      const time = Math.floor(new Date(row.dateTime).getTime() / 1000);
      const open = Number(row.openPrice);
      const high = Number(row.highPrice);
      const low = Number(row.lowPrice);
      const close = Number(row.closePrice);
      const volume = Number(row.volume || 0);

      // Validation des données pour éviter les valeurs invalides
      if (!isNaN(open) && !isNaN(high) && !isNaN(low) && !isNaN(close) && 
          open > 0 && high > 0 && low > 0 && close > 0) {
        candles.push({ time, open, high, low, close });
        line.push({ time, value: close });
        volumes.push({
          time,
          value: volume,
          color: close >= open ? '#10b98155' : '#ef444455'
        });
      }
    }

    return { candles, line, volumes };
  }

  private async loadHistory(symbol: string): Promise<PriceHistory[]> {
    if (this.historyCache.has(symbol)) {
      return this.historyCache.get(symbol)!;
    }

    const fullHistory = await this.marketApi.getPriceHistoryAll(symbol);
    const sanitized = this.ensureSorted(fullHistory);
    this.historyCache.set(symbol, sanitized);
    return sanitized;
  }

  /**
   * Trie les données historiques par date croissante
   */
  private ensureSorted(history: PriceHistory[]): PriceHistory[] {
    return [...history].sort((a, b) => 
      new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime()
    );
  }
  
  /**
   * Ajoute une mise à jour en temps réel aux données existantes
   */
  addRealTimeUpdate(update: PriceHistory): void {
    const symbol = this.currentSymbol.value;
    if (!symbol) return;
    
    const history = this.historyCache.get(symbol) || [];
    const existingIndex = history.findIndex(h => h.dateTime === update.dateTime);
    
    if (existingIndex >= 0) {
      // Mettre à jour la bougie existante
      history[existingIndex] = update;
    } else {
      // Ajouter une nouvelle bougie
      history.push(update);
      // Maintenir le tri par date
      history.sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());
    }
    
    this.historyCache.set(symbol, history);
    this.realTimeUpdates.next(update);
  }
  
  /**
   * Récupère les mises à jour en temps réel sous forme d'observable
   */
  getRealTimeUpdates(): Observable<PriceHistory> {
    return this.realTimeUpdates.asObservable();
  }

  /**
   * Mettre à jour les données de la série avec une nouvelle bougie (simulation temps réel)
   */
  updateSeriesWithNewCandle(symbol: string, newCandle: PriceHistory): void {
    const history = this.historyCache.get(symbol);
    if (!history) return;

    // Ajouter ou mettre à jour la bougie
    const existingIndex = history.findIndex(h => h.dateTime === newCandle.dateTime);
    if (existingIndex >= 0) {
      history[existingIndex] = newCandle;
    } else {
      history.push(newCandle);
      history.sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());
    }

    this.historyCache.set(symbol, history);
    this.realTimeUpdates.next(newCandle);
  }

  /**
   * Obtenir la plage de dates complètes disponibles pour un symbole
   */
  getDateRange(symbol: string): { start: Date; end: Date } | null {
    const history = this.historyCache.get(symbol);
    if (!history || history.length === 0) return null;

    const sorted = this.ensureSorted(history);
    const start = new Date(sorted[0].dateTime);
    const end = new Date(sorted[sorted.length - 1].dateTime);

    return { start, end };
  }

  private aggregateByTimeframe(history: PriceHistory[], timeframe: Timeframe): PriceHistory[] {
    if (!history.length) {
      return [];
    }

    const duration = getTimeframeDuration(timeframe);
    const bucketMap = new Map<number, PriceHistory[]>();

    for (const row of history) {
      const time = new Date(row.dateTime).getTime();
      const bucketKey = Math.floor(time / duration) * duration;
      if (!bucketMap.has(bucketKey)) {
        bucketMap.set(bucketKey, []);
      }
      bucketMap.get(bucketKey)!.push(row);
    }

    const aggregated: PriceHistory[] = [];

    for (const [bucketKey, rows] of [...bucketMap.entries()].sort((a, b) => a[0] - b[0])) {
      const openRow = rows[0];
      const closeRow = rows[rows.length - 1];
      const high = Math.max(...rows.map(r => Number(r.highPrice)));
      const low = Math.min(...rows.map(r => Number(r.lowPrice)));
      const volume = rows.reduce((sum, r) => sum + Number(r.volume || 0), 0);

      aggregated.push({
        dateTime: new Date(bucketKey).toISOString(),
        openPrice: Number(openRow.openPrice),
        highPrice: high,
        lowPrice: low,
        closePrice: Number(closeRow.closePrice),
        volume
      });
    }

    return aggregated;
  }
}
