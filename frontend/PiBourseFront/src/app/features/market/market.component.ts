import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TradeFormComponent } from '../../components/Market/trade-form/trade-form.component';
import { MarketAnalysisComponent } from '../../components/Market/market-analysis/market-analysis.component';
import { TimeControlComponent } from '../../components/Market/time-control/time-control.component';
import { TimeAcceleratorComponent } from './time-accelerator/time-accelerator.component';
import { TimeTravelTimelineComponent } from '../../components/Market/time-travel-timeline/time-travel-timeline.component';
import { SimulationTimeService } from '../../services/Market/simulation-time.service';
import { ChartModule } from './chart/chart.module';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartModule, TradeFormComponent, MarketAnalysisComponent, TimeControlComponent, TimeTravelTimelineComponent, TimeAcceleratorComponent],
  template: `
    <div class="space-y-4">
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
            <app-market-chart [symbol]="symbol"></app-market-chart>
          </div>

          <app-time-travel-timeline (monthSelected)="onTimeTravelSelected($event)"></app-time-travel-timeline>
        </div>

        <div class="space-y-4">
          <app-time-control></app-time-control>

          <app-time-accelerator></app-time-accelerator>

          <!-- Panneau d'analyse de marché -->
          <app-market-analysis></app-market-analysis>
          
          <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
            <div class="font-semibold mb-4">Passer un ordre</div>
            <app-trade-form [symbol]="symbol" (placeOrder)="onPlaceOrder($event)"></app-trade-form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class MarketComponent {
  symbols = ['AAPL','MSFT','NVDA','TSLA','GOOGL','AMZN','META'];
  symbol = 'AAPL';
  constructor(private readonly simulationTimeService: SimulationTimeService) {}

  onPlaceOrder(evt: { side: 'BUY'|'SELL'; symbol: string; quantity: number; price: number }) {
    console.log('Place order', evt);
    // TODO: brancher endpoint backend de passage d’ordre quand dispo
  }

  onTimeTravelSelected(date: Date) {
    // Receives a Date emitted from the time-travel timeline when the user selects a day
    if (!date) return;
    console.log('Time travel selected date:', date);
    // Apply locally so chart and components subscribed to SimulationTimeService update
    // Avoid setting if the simulation service already has the same date to prevent cycles
    const current = this.simulationTimeService.getCurrentSimDate();
    if (current && current.getTime() === new Date(date).getTime()) {
      // already set, do nothing
      return;
    }
    this.simulationTimeService.setCurrentSimDate(new Date(date));
  }
}
