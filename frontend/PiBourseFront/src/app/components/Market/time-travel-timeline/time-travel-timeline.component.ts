import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { TimeTravelService } from '../../../services/Market/time-travel.service';
import { TimeAcceleratorService } from '../../../services/Market/time-accelerator.service';
import { firstValueFrom } from 'rxjs';
import { TimeTravelResult } from '../../../services/Market/time-travel.interface';
import { SimulationTimeService } from '../../../services/Market/simulation-time.service';
import { Subscription } from 'rxjs';
import { TimeTravelResultsComponent } from '../time-travel-results/time-travel-results.component';
import { TokenStorageService } from '../../../services/player/token-storage.service';

@Component({
  selector: 'app-time-travel-timeline',
  standalone: true,
  imports: [CommonModule, NgClass, TimeTravelResultsComponent],
  template: `
    <div class="mt-6 p-4 rounded-xl bg-gradient-to-br from-purple-900/10 to-blue-900/10 border border-purple-500/20">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-purple-300">Chronopass Trader</h3>
        <span class="text-sm text-purple-400">{{selectedMonthLabel}}</span>
      </div>
      
      <div class="mb-4">
        <p class="text-sm text-gray-400 mb-2">Sélectionnez un mois :</p>
        <div class="flex gap-1 mb-2">
          <button 
            *ngFor="let month of periodMonths; let i = index"
            (click)="selectMonth(i)"
            class="flex-1 py-1 text-xs rounded transition-colors"
            [class.bg-purple-600]="selectedMonth === i"
            [class.text-white]="selectedMonth === i"
            [class.bg-gray-700]="selectedMonth !== i"
            [class.text-gray-300]="selectedMonth !== i"
            [class.opacity-40]="isFutureMonth(i)"
            [class.cursor-not-allowed]="isFutureMonth(i)"
            [disabled]="isFutureMonth(i)">
            {{month.shortLabel}}
          </button>
        </div>
      </div>
      
      <div class="flex gap-3">
        <button 
          (click)="startTimeTravel()"
          [disabled]="isLoading || activeSession || pendingRewind"
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
             'bg-red-500/10 text-red-400': message.type === 'error',
             'bg-blue-500/10 text-blue-200': message.type === 'info'
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
  pendingRewind = false;
  
  private readonly simulationBounds = this.simTimeService.getSimulationBounds();
  periodMonths = this.buildPeriodMonths();
  selectedMonth = 0;
  activeSession: string | null = null;
  results: TimeTravelResult | null = null;
  isLoading = false;
  message: { type: 'success' | 'error' | 'info', text: string } | null = null;
  private simSubscription?: Subscription;
  private furthestUnlockedMonthIndex = 0;
  // Month-only selection now

  get selectedMonthLabel(): string {
    const selection = this.periodMonths[this.selectedMonth];
    return `${selection.fullLabel}`;
  }

  constructor(
    private timeTravelService: TimeTravelService,
    private simTimeService: SimulationTimeService,
    private acceleratorService: TimeAcceleratorService
    , private tokenStorage: TokenStorageService
  ) {}

  ngOnInit(): void {
    this.simSubscription = this.simTimeService.currentSimDateTime$.subscribe(date => {
      if (!date) {
        return;
      }

      const clampedTime = Math.min(Math.max(date.getTime(), this.simulationBounds.start.getTime()), this.simulationBounds.end.getTime());
      const clampedDate = new Date(clampedTime);
      const monthsSinceStart = (clampedDate.getFullYear() - this.simulationBounds.start.getFullYear()) * 12 +
        (clampedDate.getMonth() - this.simulationBounds.start.getMonth());
      this.furthestUnlockedMonthIndex = Math.min(Math.max(monthsSinceStart, 0), this.periodMonths.length - 1);

      // Ramener la sélection si elle dépasse la limite
      if (this.selectedMonth > this.furthestUnlockedMonthIndex) {
        this.selectedMonth = this.furthestUnlockedMonthIndex;
      }

      // Do not emit month selection automatically on init/update to avoid forcing a time travel
      // The parent will be notified only when the user explicitly selects a month/day via UI
    });
    // NOTE: removed automatic emit on component init to prevent unintended time jumps
  }

  ngOnDestroy(): void {
    this.simSubscription?.unsubscribe();
  }

  selectMonth(index: number): void {
    if (this.isFutureMonth(index)) {
      return;
    }
    this.selectedMonth = index;
    const period = this.periodMonths[this.selectedMonth];
    const dateIso = this.buildMarketIso(period.year, period.month, 1);
    this.startTimeTravelAt(dateIso, `Voyage démarré vers ${this.selectedMonthLabel}`);
  }

  async startTimeTravel(): Promise<void> {
    const period = this.periodMonths[this.selectedMonth];
    const dateIso = this.buildMarketIso(period.year, period.month, 1);
    await this.startTimeTravelAt(dateIso, `Voyage démarré vers ${this.selectedMonthLabel}`);
  }

  async completeTimeTravel(): Promise<void> {
    if (!this.activeSession) return;
    
    this.isLoading = true;
    this.message = null;
    
    try {
      const response = await firstValueFrom(this.timeTravelService.completeSession(this.activeSession));
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
    const period = this.periodMonths[this.selectedMonth];
    const selectedDate = new Date(period.year, period.month, 1);
    this.monthSelected.emit(selectedDate);
  }

  isFutureMonth(index: number): boolean {
    return index > this.furthestUnlockedMonthIndex;
  }

  private buildPeriodMonths(): { year: number; month: number; shortLabel: string; fullLabel: string }[] {
    const monthsShort = ['JAN', 'FÉV', 'MAR', 'AVR', 'MAI', 'JUN', 'JUL', 'AOÛ', 'SEP', 'OCT', 'NOV', 'DÉC'];
    const monthsFull = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];

    const start = this.simulationBounds.start;
    const end = this.simulationBounds.end;

    const result: { year: number; month: number; shortLabel: string; fullLabel: string }[] = [];

    let cursor = new Date(start.getFullYear(), start.getMonth(), 1);
    const last = new Date(end.getFullYear(), end.getMonth(), 1);

    while (cursor <= last) {
      const year = cursor.getFullYear();
      const month = cursor.getMonth();
      result.push({
        year,
        month,
        shortLabel: monthsShort[month],
        fullLabel: `${monthsFull[month]} ${year}`
      });
      cursor = new Date(year, month + 1, 1);
    }

    return result;
  }

  private async startTimeTravelAt(dateIso: string, successText = 'TimeTravel started'): Promise<void> {
    if (this.activeSession) {
      this.message = { type: 'info', text: 'Une session est déjà active.' };
      return;
    }

    this.isLoading = true;
    this.pendingRewind = true;
    this.message = { type: 'info', text: 'Synchronisation en cours...' };

    try {
      const user = this.tokenStorage.getUser() as any;
      const playerId = user?.id ?? user?.username ?? user?.playerId;
      if (!playerId) {
        throw new Error('Identifiant du joueur introuvable. Reconnectez-vous.');
      }

      this.acceleratorService.setPendingRewind(dateIso);

      console.info('[TimeTravel] startTimeTravelAt request', { playerId, dateIso });
      const result = await this.triggerTimeTravelForDate(playerId, dateIso);

      this.activeSession = result.sessionId;
      const serverDate = new Date(result.serverDate);
      this.simTimeService.setCurrentSimDate(serverDate);
      this.acceleratorService.restartPolling();

      // notify parent and close calendar safely
      this.monthSelected.emit(serverDate);
      this.message = { type: 'success', text: successText };
    } catch (err: any) {
      console.error('[TimeTravel] startTimeTravelAt error:', err);
      const reason = err?.message || 'TimeTravel failed';
      this.message = { type: 'error', text: `Erreur backend: ${reason}` };
    } finally {
      this.pendingRewind = false;
      this.acceleratorService.setPendingRewind(null);
      this.isLoading = false;
    }
  }

  /** Build market timestamp at 09:30 UTC, formatted for Spring LocalDateTime (yyyy-MM-dd'T'HH:mm:ss). */
  private buildMarketIso(year: number, month: number, day: number): string {
    const d = new Date(Date.UTC(year, month, day, 9, 30, 0));
    return this.toLocalDateTimeString(d);
  }

  /** Format a Date as yyyy-MM-ddTHH:mm:ss using UTC fields (compatible with LocalDateTime). */
  private toLocalDateTimeString(date: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${date.getUTCFullYear()}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCDate())}` +
           `T${pad(date.getUTCHours())}:${pad(date.getUTCMinutes())}:${pad(date.getUTCSeconds())}`;
  }

  private async triggerTimeTravelForDate(playerId: string, dateIso: string): Promise<{ serverDate: string; sessionId: string | null }> {
    // dateIso is already formatted as yyyy-MM-ddTHH:mm:ss (LocalDateTime compatible)
    // Send it directly without re-parsing to avoid timezone issues
    console.info('[TimeTravel] calling backend with', { playerId, dateIso });
    
    // First, stop any existing active sessions to avoid "Maximum sessions reached" error
    await this.stopExistingSessions(playerId);
    
    const response = await firstValueFrom(this.acceleratorService.startTimeTravelWithSync(playerId, dateIso));
    
    if (!response || response.status === 'error') {
      throw new Error(response?.message || 'Démarrage refusé');
    }
    
    const serverDate = response.currentSimulationDate || response.rewindToDate;
    if (!serverDate) {
      throw new Error('La synchronisation n\'a pas renvoyé de date');
    }
    
    return { serverDate, sessionId: response.sessionId || null };
  }

  /** Stop all existing active sessions for the player to avoid session limit errors */
  private async stopExistingSessions(playerId: string): Promise<void> {
    try {
      const sessions = await firstValueFrom(this.acceleratorService.getPlayerTimeTravelSessions(playerId));
      if (sessions && Array.isArray(sessions)) {
        const activeSessions = sessions.filter((s: any) => s.status === 'ACTIVE');
        for (const session of activeSessions) {
          try {
            console.info('[TimeTravel] Stopping existing session:', session.sessionId);
            await firstValueFrom(this.timeTravelService.stopSession(session.sessionId));
          } catch (err) {
            console.warn('[TimeTravel] Failed to stop session:', session.sessionId, err);
          }
        }
      }
    } catch (err) {
      console.warn('[TimeTravel] Could not fetch existing sessions:', err);
      // Continue anyway - the backend might still accept the new session
    }
  }

  async stopCurrentSession(): Promise<void> {
    if (!this.activeSession) return;
    this.isLoading = true;
    try {
      const resp = await firstValueFrom(this.timeTravelService.stopSession(this.activeSession));
      this.message = { type: 'success', text: resp.message || 'Session arrêtée' };
      this.activeSession = null;
      // reset accelerator state by asking backend to reset if needed
      await firstValueFrom(this.acceleratorService.resetAcceleration());
      this.acceleratorService.restartPolling();
      // clear pending rewind if any
      this.pendingRewind = false;
      this.acceleratorService.setPendingRewind(null);
    } catch (err) {
      console.error('Error stopping session:', err);
      this.message = { type: 'error', text: 'Erreur lors de l\'arrêt de la session' };
    } finally {
      this.isLoading = false;
    }
  }
}
