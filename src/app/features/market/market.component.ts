import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CandlestickChartComponent } from '../../components/Market/candlestick-chart/candlestick-chart.component';
import { TradeFormComponent } from '../../components/Market/trade-form/trade-form.component';
import { TimeControlComponent } from '../../components/Market/time-control/time-control.component';
import { MarketAnalysisComponent } from '../../components/Market/market-analysis/market-analysis.component';
import { TimeTravelTimelineComponent } from '../../components/Market/time-travel-timeline/time-travel-timeline.component';
import { TimeAcceleratorService } from '../../services/Market/time-accelerator.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule, FormsModule, CandlestickChartComponent, TradeFormComponent, TimeControlComponent, MarketAnalysisComponent, TimeTravelTimelineComponent],
  template: `
    <div class="grid xl:grid-cols-3 gap-4">
      <div class="xl:col-span-2 space-y-4 min-w-0">
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="flex flex-wrap items-center gap-3 mb-3">
            <div class="font-semibold">Marché en direct</div>
            <div class="ml-auto flex items-center gap-2">
              <label class="text-sm text-gray-600 dark:text-gray-400">Symbole</label>
              <select class="px-2 py-1 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-sm" [(ngModel)]="symbol">
                <option *ngFor="let s of symbols" [value]="s">{{s}}</option>
              </select>
            </div>
          </div>
          <app-candlestick-chart 
            [symbol]="symbol" 
            [startDate]="startISO" 
            [endDate]="endISO"
            [height]="480">
          </app-candlestick-chart>

          <app-time-travel-timeline (monthSelected)="onMonthSelected($event)"></app-time-travel-timeline>

          <div class="mt-3 flex items-center justify-between text-sm">
            <div class="flex items-center gap-3">
              <div class="text-gray-600 dark:text-gray-400">
                📅 Date de simulation: <span class="font-semibold text-gray-800 dark:text-gray-200">{{currentSimDate | date:'dd/MM/yyyy HH:mm'}}</span>
              </div>
              <div class="px-2 py-1 rounded bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-xs font-medium">
                ⚡ LIVE
              </div>
            </div>
            <div class="text-gray-500 text-xs">
              Mode trading réaliste • Données historiques 2023
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <!-- Panneau de contrôle du temps -->
        <app-time-control></app-time-control>
        
        <!-- Panneau d'analyse de marché -->
        <app-market-analysis></app-market-analysis>
        
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold mb-4">Passer un ordre</div>
          <app-trade-form [symbol]="symbol" (placeOrder)="onPlaceOrder($event)"></app-trade-form>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class MarketComponent implements OnInit, OnDestroy {
  symbols = ['AAPL','MSFT','NVDA','TSLA','GOOGL','AMZN','META'];
  symbol = 'AAPL';
  currentSimDate = new Date('2023-02-01T09:30:00');
  private selectedMonthStart: Date | null = null;
  private selectedMonthEndBoundary: Date | null = null;
  
  private simDateSubscription?: Subscription;

  constructor(private timeAccelerator: TimeAcceleratorService) {}

  ngOnInit() {
    // S'abonner aux changements de date de simulation depuis le backend
    this.simDateSubscription = this.timeAccelerator.getCurrentGameTime().subscribe((date: Date | null) => {
      if (date) {
        this.currentSimDate = date;
      }
    });
  }

  ngOnDestroy() {
    this.simDateSubscription?.unsubscribe();
  }
  
  // Pour le graphique, on affiche les 30 derniers jours jusqu'à "maintenant"
  get startISO() { 
    const start = this.selectedMonthStart ?? this.startOfDay(this.addDays(this.currentSimDate, -30));
    return this.toLocalISOStringNoTZ(start); 
  }
  
  get endISO() { 
    const monthBoundary = this.selectedMonthEndBoundary;
    const effectiveEnd = monthBoundary && this.currentSimDate.getTime() > monthBoundary.getTime()
      ? monthBoundary
      : this.currentSimDate;
    return this.toLocalISOStringNoTZ(effectiveEnd); 
  }

  onMonthSelected(date: Date) {
    const start = this.startOfMonth(date);
    const end = this.endOfMonth(date);
    this.selectedMonthStart = start;
    this.selectedMonthEndBoundary = end;
    this.currentSimDate = start;
  }

  private addDays(d: Date, n: number) {
    const x = new Date(d);
    x.setDate(x.getDate() + n);
    return x;
  }

  private toLocalISOStringNoTZ(d: Date) {
    const y = d.getFullYear();
    const m = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const hh = d.getHours().toString().padStart(2, '0');
    const mm = d.getMinutes().toString().padStart(2, '0');
    const ss = d.getSeconds().toString().padStart(2, '0');
    return `${y}-${m}-${day}T${hh}:${mm}:${ss}`;
  }

  private startOfDay(d: Date) {
    const x = new Date(d);
    x.setHours(0,0,0,0);
    return x;
  }

  private endOfDay(d: Date) {
    const x = new Date(d);
    x.setHours(23,59,59,999);
    return x;
  }

  private startOfMonth(d: Date) {
    const start = new Date(d.getFullYear(), d.getMonth(), 1);
    return this.startOfDay(start);
  }

  private endOfMonth(d: Date) {
    const end = new Date(d.getFullYear(), d.getMonth() + 1, 0);
    return this.endOfDay(end);
  }

  onPlaceOrder(evt: { side: 'BUY'|'SELL'; symbol: string; quantity: number; price: number }) {
    console.log('Place order', evt);
    // TODO: brancher endpoint backend de passage d’ordre quand dispo
  }
}
