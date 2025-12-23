import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { MarketApiService } from '../../../services/Market/market-api.service';
import { PriceHistory } from '../../../models/Market/price-history.model';
import { SimulationTimeService } from '../../../services/Market/simulation-time.service';

@Component({
  selector: 'app-candlestick-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="w-full relative">
      <!-- Header avec symbole et timeframes -->
      <div class="mb-3 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="text-lg font-bold text-gray-100">{{symbol}}</div>
          <div class="text-sm text-gray-400">{{startDate | date:'yyyy-MM-dd'}} → {{endDate | date:'yyyy-MM-dd'}}</div>
          <div *ngIf="loading" class="text-xs text-cyan-400 animate-pulse">⟳ Chargement...</div>
          <div *ngIf="error" class="text-xs text-red-400">⚠ {{error}}</div>
        </div>
        
        <!-- Timeframes MetaTrader -->
        <div class="flex items-center gap-1 bg-gray-900/50 rounded-lg p-1 border border-gray-700/50">
          <button *ngFor="let tf of timeframes" (click)="selectTimeframe(tf)" [ngClass]="{'bg-cyan-500/20 text-cyan-400 border-cyan-500/30': selectedTimeframe === tf}" class="px-2.5 py-1 text-xs font-medium rounded border border-transparent text-gray-400 hover:bg-gray-800/50 hover:text-gray-200 transition-all">{{tf}}</button>
        </div>
      </div>
      
      <!-- Graphique container -->
      <div class="relative rounded-xl overflow-hidden border border-gray-800/80 shadow-2xl">
        <div #container 
             class="w-full bg-gradient-to-b from-gray-950 to-black" 
             [style.height.px]="height">
        </div>
        
        <!-- Overlay pour données manquantes -->
        <div *ngIf="!loading && !error && !hasData" 
             class="absolute inset-0 flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <div class="text-center">
            <div class="text-4xl mb-2">📊</div>
            <div class="text-sm text-gray-300 font-medium">Aucune donnée disponible</div>
            <div class="text-xs text-gray-500 mt-1">pour cette période</div>
          </div>
        </div>
        
        <!-- Loading overlay -->
        <div *ngIf="loading" 
             class="absolute inset-0 flex items-center justify-center bg-black/70 backdrop-blur-sm">
          <div class="text-center">
            <div class="inline-block animate-spin text-4xl mb-2">⟳</div>
            <div class="text-sm text-cyan-400 font-medium">Chargement des données...</div>
          </div>
        </div>
      </div>
      
      <!-- Légende et infos -->
      <div class="mt-2 flex items-center justify-between text-xs text-gray-500">
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-1.5">
            <span class="w-3 h-3 rounded-sm bg-gradient-to-br from-cyan-400 to-emerald-500"></span>
            <span>Haussier</span>
          </div>
          <div class="flex items-center gap-1.5">
            <span class="w-3 h-3 rounded-sm bg-gradient-to-br from-red-500 to-pink-600"></span>
            <span>Baissier</span>
          </div>
        </div>
        <div class="text-gray-600">
          Glisser pour déplacer • Molette pour zoomer
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
    
    .animate-fadeIn {
      animation: fadeIn 0.3s ease-out;
    }
  `]
})
export class CandlestickChartComponent implements OnInit, OnChanges, OnDestroy {
  @Input() symbol = 'AAPL';
  @Input() startDate!: string; // ISO string
  @Input() endDate!: string;   // ISO string
  @Input() height = 480;

  @ViewChild('container', { static: true }) containerRef!: ElementRef<HTMLDivElement>;

  loading = false;
  error: string | null = null;

  // Timeframes adaptés aux données journalières
  timeframes = ['1M', '3M', '6M', 'YTD'];  
  // 1M = 1 mois, 3M = 3 mois, 6M = 6 mois, YTD = Année complète
  selectedTimeframe = '1M';

  private chart: any = null;
  private candleSeries: any = null;
  private volumeSeries: any = null;
  private resizeObserver?: ResizeObserver;
  private retried = false;
  private isChartReady = false;
  hasData = false;

  constructor(
    private api: MarketApiService,
    private simTimeService: SimulationTimeService
  ) {}

  async ngOnInit() {
    try {
      await this.initChart();
      if (this.isChartReady) {
        await this.loadData();
      }
    } catch (err) {
      console.error('[Chart Init Error]', err);
      this.error = 'Erreur d\'initialisation du graphique';
    }
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (this.isChartReady && (changes['symbol'] || changes['startDate'] || changes['endDate'])) {
      this.retried = false;
      await this.loadData();
    }
  }

  ngOnDestroy(): void {
    this.cleanup();
  }

  selectTimeframe(tf: string) {
    this.selectedTimeframe = tf;
    console.log('[Chart] Timeframe selected:', tf);
    
    // Adapter le barSpacing selon le timeframe pour un affichage optimal
    if (this.chart) {
      let barSpacing = 4;
      let minBarSpacing = 1;
      
      switch(tf) {
        case '1M':
          barSpacing = 4;  // Vue 1 mois - espacement confortable
          minBarSpacing = 1.5;
          break;
        case '3M':
          barSpacing = 3;  // Vue 3 mois - moyennement serré
          minBarSpacing = 1;
          break;
        case '6M':
          barSpacing = 2;  // Vue 6 mois - serré
          minBarSpacing = 0.8;
          break;
        case 'YTD':
          barSpacing = 1.5;  // Vue année - ultra-serré pour tout voir
          minBarSpacing = 0.5;
          break;
      }
      
      // Animation fluide lors du changement de timeframe
      this.chart.applyOptions({
        timeScale: {
          barSpacing: barSpacing,
          minBarSpacing: minBarSpacing,
        }
      });
      
      // Refit le contenu avec animation douce
      setTimeout(() => {
        if (this.chart && this.isChartReady) {
          this.chart.timeScale().fitContent();
          console.log(`[Chart] Timeframe ${tf} applied - barSpacing: ${barSpacing}`);
        }
      }, 150);
    }
  }

  private cleanup() {
    try {
      if (this.resizeObserver) {
        this.resizeObserver.disconnect();
        this.resizeObserver = undefined;
      }
      if (this.chart && typeof this.chart.remove === 'function') {
        this.chart.remove();
      }
    } catch (err) {
      console.warn('[Chart Cleanup]', err);
    } finally {
      this.chart = null;
      this.candleSeries = null;
      this.volumeSeries = null;
      this.isChartReady = false;
    }
  }

  private async initChart(): Promise<void> {
    const container = this.containerRef?.nativeElement;
    if (!container) {
      throw new Error('Container element not found');
    }

    // Cleanup existing chart
    if (this.chart) {
      this.cleanup();
    }

    try {
      // Load the library from CDN
      const LW = await this.loadLightweightChartsLib();
      
      const width = container.clientWidth || 800;
      const height = this.height || 480;

      console.debug('[Chart] Creating chart:', width, 'x', height);

      // Create chart avec paramètres SUBLIME style MetaTrader Pro
      this.chart = LW.createChart(container, {
        layout: { 
          background: { type: LW.ColorType.Solid, color: '#000000' },  // Noir pur
          textColor: '#64748b',  // Gris doux
          fontSize: 11,
          fontFamily: "'Inter', 'Roboto', -apple-system, sans-serif",
        },
        grid: { 
          vertLines: { 
            color: '#1e293b',  // Grille très discrète
            style: 0,  // Solid
            visible: true 
          }, 
          horzLines: { 
            color: '#1e293b',
            style: 0,
            visible: true
          } 
        },
        timeScale: { 
          rightOffset: 8,
          barSpacing: 4,  // Bougies ultra-fines
          minBarSpacing: 1,
          fixLeftEdge: false,
          fixRightEdge: false,
          lockVisibleTimeRangeOnResize: true,
          timeVisible: true,
          secondsVisible: false,
          borderVisible: false,
          borderColor: '#2d3748',
        },
        crosshair: { 
          mode: LW.CrosshairMode.Normal,
          vertLine: {
            color: '#06b6d480',  // Cyan semi-transparent
            width: 1,
            style: 2,  // Dashed
            labelBackgroundColor: '#06b6d4',
            labelVisible: true
          },
          horzLine: {
            color: '#06b6d480',
            width: 1,
            style: 2,
            labelBackgroundColor: '#06b6d4',
            labelVisible: true
          }
        },
        rightPriceScale: { 
          borderVisible: false,
          borderColor: '#2d3748',
          scaleMargins: { top: 0.12, bottom: 0.25 },
          textColor: '#64748b',
          entireTextOnly: false,
          visible: true,
          alignLabels: true,
          autoScale: true
        },
        leftPriceScale: {
          visible: false
        },
        width,
        height,
        localization: { 
          dateFormat: 'yyyy-MM-dd',
          timeFormatter: (time: any) => {
            const date = new Date(time * 1000);
            return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
          }
        },
        handleScroll: {
          mouseWheel: true,
          pressedMouseMove: true,
          horzTouchDrag: true,
          vertTouchDrag: false,
        },
        handleScale: {
          axisPressedMouseMove: true,
          mouseWheel: true,
          pinch: true,
        },
      });

      if (!this.chart) {
        throw new Error('Chart creation failed');
      }

      console.debug('[Chart] Chart created, adding series...');

      // Add candlestick series SUBLIME avec couleurs vibrantes
      this.candleSeries = this.chart.addCandlestickSeries({
        upColor: '#10b981',  // Émeraude lumineux
        downColor: '#ef4444',  // Rouge vif
        borderUpColor: '#10b981',
        borderDownColor: '#ef4444',
        wickUpColor: '#10b981',
        wickDownColor: '#ef4444',
        borderVisible: false,
        priceLineVisible: true,
        priceLineWidth: 1,
        priceLineColor: '#06b6d4',
        priceLineStyle: 2,
        lastValueVisible: true,
        priceFormat: {
          type: 'price',
          precision: 2,
          minMove: 0.01,
        },
      });

      // Add volume series ultra-discrète
      this.volumeSeries = this.chart.addHistogramSeries({
        priceFormat: { 
          type: 'volume',
        }, 
        priceScaleId: '',  // Overlay
        color: '#475569',  // Gris très discret
        priceLineVisible: false,
        lastValueVisible: false,
        scaleMargins: { top: 0.75, bottom: 0 },
        base: 0,
      });

      console.debug('[Chart] Series created successfully');

      // Setup resize observer
      this.resizeObserver = new ResizeObserver(() => {
        if (this.chart && this.isChartReady) {
          try {
            const w = container.clientWidth || width;
            this.chart.applyOptions({ width: w, height: this.height });
          } catch (err) {
            console.warn('[Chart Resize]', err);
          }
        }
      });
      this.resizeObserver.observe(container);

      this.isChartReady = true;
      console.debug('[Chart] Init complete ✓');

    } catch (err) {
      console.error('[Chart Init Error]', err);
      this.cleanup();
      throw err;
    }
  }

  private loadLightweightChartsLib(): Promise<any> {
    return new Promise((resolve, reject) => {
      const w = window as any;
      
      // Check if already loaded
      if (w.LightweightCharts) {
        console.debug('[Chart] Library already loaded');
        resolve(w.LightweightCharts);
        return;
      }

      // Check if script tag already exists
      const existing = document.querySelector('script[src*="lightweight-charts"]');
      if (existing) {
        // Wait for it to load
        const checkInterval = setInterval(() => {
          if (w.LightweightCharts) {
            clearInterval(checkInterval);
            console.debug('[Chart] Library loaded from existing script');
            resolve(w.LightweightCharts);
          }
        }, 50);
        
        setTimeout(() => {
          clearInterval(checkInterval);
          if (!w.LightweightCharts) {
            reject(new Error('Timeout waiting for library'));
          }
        }, 5000);
        return;
      }

      // Load script
      console.debug('[Chart] Loading library from CDN...');
      const script = document.createElement('script');
      script.src = 'https://unpkg.com/lightweight-charts@4.2.1/dist/lightweight-charts.standalone.production.js';
      script.async = false; // Load synchronously
      
      script.onload = () => {
        console.debug('[Chart] Script loaded, checking...');
        if (w.LightweightCharts) {
          console.debug('[Chart] Library ready ✓');
          resolve(w.LightweightCharts);
        } else {
          reject(new Error('Library not available after script load'));
        }
      };
      
      script.onerror = () => {
        reject(new Error('Failed to load library script'));
      };
      
      document.head.appendChild(script);
    });
  }

  private async loadData() {
    try {
      this.loading = true;
      this.error = null;

      // Ensure chart is ready
      if (!this.isChartReady || !this.chart || !this.candleSeries || !this.volumeSeries) {
        await this.initChart();
      }

      // Verify again after init
      if (!this.isChartReady || !this.candleSeries || !this.volumeSeries) {
        throw new Error('Chart not ready after initialization');
      }

      // Fetch data
      const data: PriceHistory[] = await this.api.getPriceHistory(this.symbol, this.startDate, this.endDate);
      const { candles, volumes } = this.toSeriesData(data);
      console.debug('[Chart]', this.symbol, 'points:', candles.length, this.startDate, this.endDate);
      this.hasData = candles.length > 0;

      // Simple retry: if empty, try extending end by +7 days once
      if (!this.hasData && !this.retried) {
        this.retried = true;
        const endPlus7 = this.addDays(new Date(this.endDate), 7).toISOString().slice(0,19);
        const retryData: PriceHistory[] = await this.api.getPriceHistory(this.symbol, this.startDate, endPlus7);
        const retry = this.toSeriesData(retryData);
        this.hasData = retry.candles.length > 0;
        if (this.hasData) {
          this.setChartData(retry.candles, retry.volumes);
          return;
        }
      }

      // Fallback: fetch full history for the symbol
      if (!this.hasData) {
        const allData: PriceHistory[] = await this.api.getPriceHistoryAll(this.symbol);
        const all = this.toSeriesData(allData);
        this.hasData = all.candles.length > 0;
        if (this.hasData) {
          this.setChartData(all.candles, all.volumes);
          return;
        }
      }

      // Set whatever data we have
      this.setChartData(candles || [], volumes || []);

    } catch (e: any) {
      console.error('[Chart Load Error]', e);
      this.error = e?.message || 'Erreur de chargement';
    } finally {
      this.loading = false;
    }
  }

  private setChartData(candles: any[], volumes: any[]) {
    if (!this.isChartReady || !this.candleSeries || !this.volumeSeries || !this.chart) {
      console.warn('[Chart] Cannot set data - chart not ready');
      return;
    }

    try {
      this.candleSeries.setData(candles);
      this.volumeSeries.setData(volumes);
      
      // Fit content with a small delay to ensure rendering is complete
      setTimeout(() => {
        if (this.chart && this.isChartReady) {
          try {
            this.chart.timeScale().fitContent();
          } catch (err) {
            console.warn('[Chart FitContent]', err);
          }
        }
      }, 50);
    } catch (err) {
      console.error('[Chart SetData]', err);
      throw new Error('Failed to set chart data');
    }
  }

  private toSeriesData(ph: PriceHistory[]) {
    const candles: any[] = [];
    const volumes: any[] = [];
    for (const r of ph) {
      const t = Math.floor(new Date(r.dateTime).getTime() / 1000);
      const o = Number(r.openPrice), h = Number(r.highPrice), l = Number(r.lowPrice), c = Number(r.closePrice);
      candles.push({ time: t, open: o, high: h, low: l, close: c });
      // Couleurs sublimes avec transparence pour volumes discrets
      volumes.push({ 
        time: t, 
        value: Number(r.volume), 
        color: c >= o ? '#10b98150' : '#ef444450'  // Très transparent
      });
    }
    return { candles, volumes };
  }

  private addDays(d: Date, n: number) {
    const x = new Date(d);
    x.setDate(x.getDate() + n);
    return x;
  }
}
