import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { TimeTravelService } from '../../../services/Market/time-travel.service';
import { TimeTravelResult } from '../../../services/Market/time-travel.interface';
import { SimulationTimeService } from '../../../services/Market/simulation-time.service';
import { Subscription } from 'rxjs';
import { TimeTravelResultsComponent } from '../time-travel-results/time-travel-results.component';

@Component({
  selector: 'app-time-travel-timeline',
  standalone: true,
  imports: [CommonModule, NgClass, TimeTravelResultsComponent],
  template: `
    <div class="mt-6 p-4 rounded-xl bg-gradient-to-br from-purple-900/10 to-blue-900/10 border border-purple-500/20">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-purple-300">Chronopass Trader</h3>
        <span class="text-sm text-purple-400">{{selectedMonthName}} 2023</span>
      </div>
      
      <div class="mb-4">
        <p class="text-sm text-gray-400 mb-2">Sélectionnez un mois :</p>
        <div class="flex gap-1 mb-2">
          <button 
            *ngFor="let month of months; let i = index"
            (click)="selectMonth(i)"
            class="flex-1 py-1 text-xs rounded transition-colors"
            [class.bg-purple-600]="selectedMonth === i"
            [class.text-white]="selectedMonth === i"
            [class.bg-gray-700]="selectedMonth !== i"
            [class.text-gray-300]="selectedMonth !== i"
            [class.opacity-40]="isFutureMonth(i)"
            [class.cursor-not-allowed]="isFutureMonth(i)"
            [disabled]="isFutureMonth(i)">
            {{month.short}}
          </button>
        </div>
      </div>
      
      <div class="flex gap-3">
        <button 
          (click)="startTimeTravel()"
          [disabled]="isLoading || activeSession"
          class="flex-1 py-2 px-4 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          {{ activeSession ? 'Session active' : 'Démarrer' }}
        </button>
        
        <button 
          (click)="completeTimeTravel()"
          [disabled]="!activeSession || isLoading"
          class="flex-1 py-2 px-4 bg-cyan-600 hover:bg-cyan-500 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          Résultats
        </button>
      </div>
      
      <div *ngIf="message" class="mt-3 p-3 rounded-lg text-sm" 
           [ngClass]="{
             'bg-emerald-500/10 text-emerald-400': message.type === 'success',
             'bg-red-500/10 text-red-400': message.type === 'error'
           }">
        {{message.text}}
      </div>

      <app-time-travel-results 
        *ngIf="showResultsModal && results"
        [results]="results"
        (close)="closeResults()">
      </app-time-travel-results>
    </div>
  `
})
export class TimeTravelTimelineComponent implements OnInit, OnDestroy {
  @Output() monthSelected = new EventEmitter<Date>();
  
  showResultsModal = false;
  
  months = [
    { short: 'JAN', name: 'Janvier' },
    { short: 'FÉV', name: 'Février' },
    { short: 'MAR', name: 'Mars' },
    { short: 'AVR', name: 'Avril' },
    { short: 'MAI', name: 'Mai' },
    { short: 'JUN', name: 'Juin' },
    { short: 'JUL', name: 'Juillet' },
    { short: 'AOÛ', name: 'Août' },
    { short: 'SEP', name: 'Septembre' },
    { short: 'OCT', name: 'Octobre' },
    { short: 'NOV', name: 'Novembre' },
    { short: 'DÉC', name: 'Décembre' }
  ];
  
  selectedMonth = 0;
  activeSession: string | null = null;
  results: TimeTravelResult | null = null;
  isLoading = false;
  message: { type: 'success' | 'error', text: string } | null = null;
  private simSubscription?: Subscription;
  private furthestUnlockedMonthIndex = 0;

  get selectedMonthName(): string {
    return this.months[this.selectedMonth].name;
  }

  constructor(
    private timeTravelService: TimeTravelService,
    private simTimeService: SimulationTimeService
  ) {}

  ngOnInit(): void {
    this.simSubscription = this.simTimeService.currentSimDateTime$.subscribe(date => {
      if (!date) {
        return;
      }

      const monthIndex = date.getMonth();
      this.furthestUnlockedMonthIndex = Math.min(monthIndex, this.months.length - 1);

      // Ramener la sélection si elle dépasse la limite
      if (this.selectedMonth > this.furthestUnlockedMonthIndex) {
        this.selectedMonth = this.furthestUnlockedMonthIndex;
      }

      this.emitSelectedMonth();
    });

    this.emitSelectedMonth();
  }

  ngOnDestroy(): void {
    this.simSubscription?.unsubscribe();
  }

  selectMonth(index: number): void {
    if (this.isFutureMonth(index)) {
      return;
    }
    this.selectedMonth = index;
    this.emitSelectedMonth();
  }

  async startTimeTravel(): Promise<void> {
    if (this.activeSession) return;
    
    this.isLoading = true;
    this.message = null;
    
    try {
      const date = new Date(2023, this.selectedMonth, 1).toISOString();
      const response = await this.timeTravelService.startSession('player-001', date).toPromise();
      
      this.activeSession = response.sessionId;
      this.message = {
        type: 'success',
        text: `Voyage démarré vers ${this.selectedMonthName} 2023`
      };
    } catch (error) {
      console.error('Error starting time travel:', error);
      this.message = {
        type: 'error',
        text: 'Erreur lors du démarrage du voyage'
      };
    } finally {
      this.isLoading = false;
    }
  }

  async completeTimeTravel(): Promise<void> {
    if (!this.activeSession) return;
    
    this.isLoading = true;
    this.message = null;
    
    try {
      const response = await this.timeTravelService.completeSession(this.activeSession).toPromise();
      this.results = response.results;
      this.showResultsModal = true;
    } catch (error) {
      console.error('Error completing time travel:', error);
      this.message = {
        type: 'error',
        text: 'Erreur lors de la récupération des résultats'
      };
    } finally {
      this.isLoading = false;
    }
  }
  
  closeResults(): void {
    this.showResultsModal = false;
    this.results = null;
  }

  private emitSelectedMonth(): void {
    const selectedDate = new Date(2023, this.selectedMonth, 1);
    this.monthSelected.emit(selectedDate);
  }

  isFutureMonth(index: number): boolean {
    return index > this.furthestUnlockedMonthIndex;
  }
}
