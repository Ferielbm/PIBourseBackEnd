import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TimeAcceleratorService } from '../../../services/Market/time-accelerator.service';
import { SimulationTimeService } from '../../../services/Market/simulation-time.service';
import { TokenStorageService } from '../../../services/player/token-storage.service';
import { Observable, firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-time-accelerator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
      <div class="flex items-center justify-between gap-4">
        <div class="flex flex-col">
          <div class="text-sm font-semibold text-gray-800 dark:text-gray-100">Temps de simulation</div>
          <div class="mt-1 text-xs text-gray-600 dark:text-gray-300">{{ (currentGameTime$ | async) ? (currentGameTime$ | async) : '—' }}</div>
          <div class="mt-1 text-xs text-gray-600 dark:text-gray-300">Compression: <span class="font-medium text-sky-500">{{ (compressionInfo$ | async) || '—' }}</span></div>
        </div>

        <div class="flex items-center gap-2">
          <button class="px-3 py-1 rounded-md border border-sky-500 bg-sky-600 text-white text-sm hover:opacity-90" (click)="onToggle()">
            {{ (isActive$ | async) ? 'Arrêter' : 'Démarrer' }}
          </button>

          <!-- TimeTravel is handled by a separate component -->

          <button class="px-3 py-1 rounded-md border bg-transparent text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-800" (click)="onReset()">Réinitialiser</button>

          <button *ngIf="isGameMaster" class="ml-2 inline-flex items-center gap-2 px-3 py-1 rounded-md bg-gradient-to-r from-sky-500 to-cyan-400 text-white shadow-md hover:opacity-95" (click)="openEdit()" title="Modifier la minuterie">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z" fill="white"/><path d="M20.71 7.04a1.003 1.003 0 0 0 0-1.42l-2.34-2.34a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.84-1.82z" fill="white"/></svg>
            <span class="text-sm">Modifier</span>
          </button>
        </div>
      </div>
    </div>
    <div *ngIf="errorMessage" class="mt-2 text-sm text-red-400">{{errorMessage}}</div>

    <!-- Inline Modal (centered, backdrop blur like MarketAnalysis) -->
    <div *ngIf="showEditModal && isGameMaster" (click)="closeEdit()" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fade-in">
      <div (click)="$event.stopPropagation()" class="bg-gray-900 rounded-2xl border border-gray-700 shadow-2xl max-w-md w-full overflow-hidden animate-scale-in">
        <div class="bg-gradient-to-r from-cyan-900/50 to-blue-900/50 p-4 border-b border-gray-700 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="text-2xl">⏱️</div>
            <div>
              <div class="font-bold text-white text-lg">Modifier la minuterie</div>
              <div class="text-xs text-gray-400">Ajuster la durée réelle pour simuler 15 mois</div>
            </div>
          </div>
          <button (click)="closeEdit()" class="text-gray-400 hover:text-white transition-colors text-2xl leading-none">×</button>
        </div>

        <div class="p-6">
          <div class="mb-3 text-sm text-gray-300">Durée réelle (minutes) pour simuler 15 mois</div>
          <input type="number" [(ngModel)]="editMinutes" min="1" max="240" class="w-full px-3 py-2 rounded-md border border-gray-700 bg-gray-800 text-white mb-3" />

          <div class="flex gap-2 mb-4">
            <button class="px-3 py-1 rounded-md bg-gray-800 text-sm text-gray-200 border" (click)="editMinutes=15">15 min</button>
            <button class="px-3 py-1 rounded-md bg-gray-800 text-sm text-gray-200 border" (click)="editMinutes=30">30 min</button>
            <button class="px-3 py-1 rounded-md bg-gray-800 text-sm text-gray-200 border" (click)="editMinutes=45">45 min</button>
          </div>

          <div class="flex justify-end gap-3">
            <button class="px-4 py-2 rounded-md border text-sm" (click)="closeEdit()">Annuler</button>
            <button class="px-4 py-2 rounded-md bg-sky-600 text-white text-sm" (click)="saveEdit()">Enregistrer</button>
          </div>
        </div>
      </div>
    </div>

    
  `,
  styles: [
    `
    @keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }
    @keyframes scale-in { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
    .animate-fade-in { animation: fade-in 0.18s ease-out; }
    .animate-scale-in { animation: scale-in 0.22s ease-out; }
    `
  ]
})
export class TimeAcceleratorComponent implements OnInit {
  currentGameTime$!: Observable<Date | null>;
  isActive$!: Observable<boolean>;
  compressionInfo$!: Observable<string>;
  isGameMaster = false;
  errorMessage: string | null = null;

  showEditModal = false;
  editMinutes = 30;
  // TimeTravel is handled by a separate component

  constructor(
    private readonly accelerator: TimeAcceleratorService,
    private readonly tokenStorage: TokenStorageService,
    private readonly simulationTimeService: SimulationTimeService
  ) {}

  ngOnInit(): void {
    this.currentGameTime$ = this.accelerator.getCurrentGameTime();
    this.isActive$ = this.accelerator.isAccelerationActive();
    this.compressionInfo$ = this.accelerator.getCompressionInfo();

    const user = this.tokenStorage.getUser() as any;
    this.isGameMaster = !!(user && (user.role === 'GAME_MASTER' || user.role === 'ROLE_GAME_MASTER' || user.role === 'ADMIN'));
  }

  async onToggle(): Promise<void> {
    this.errorMessage = null;
    try {
      const active = await firstValueFrom(this.accelerator.isAccelerationActive());
      if (active) {
        this.accelerator.stopAcceleration().subscribe({
          next: () => {},
          error: (err) => {
            console.error('Error stopping accelerator:', err);
            this.simulationTimeService.stopSimulation();
            this.errorMessage = 'Impossible d\'arrêter le time-accelerator côté serveur. Mode local activé.';
          }
        });
      } else {
        try {
          // Ensure server game time is not earlier than our default simulation start
          const backendTime = await firstValueFrom(this.accelerator.getCurrentGameTime());
          const bounds = this.simulationTimeService.getSimulationBounds();
          const defaultStart = bounds.defaultStart;
          if (backendTime && backendTime.getTime() < defaultStart.getTime()) {
            // attempt to set server time to default start before starting
            try {
              await firstValueFrom(this.accelerator.setServerGameTime(defaultStart.toISOString()));
            } catch (err) {
              console.warn('Unable to set server time before starting accelerator, continuing to start:', err);
            }
          }

          // attempt to start accelerator on server
          await firstValueFrom(this.accelerator.startAcceleration());
        } catch (err) {
          console.error('Error starting accelerator:', err);
          // fallback to local simulation
          this.simulationTimeService.startSimulation();
          this.errorMessage = 'Démarrage serveur impossible (401 ou erreur). Simulation locale activée.';
        }
      }
    } catch (err) {
      console.error('Error reading accelerator state:', err);
      // As a safe fallback, start local simulation
      this.simulationTimeService.startSimulation();
      this.errorMessage = 'Impossible de lire l\'état de l\'accélérateur. Simulation locale activée.';
    }
  }

  onReset(): void {
    this.accelerator.resetAcceleration().subscribe();
  }

  openEdit(): void {
    // initialize editMinutes from compressionInfo if possible
    this.editMinutes = 30;
    this.compressionInfo$.subscribe(info => {
      const m = info && info.match ? info.match(/(\d+)\s*minutes?/) : null;
      if (m && m[1]) this.editMinutes = parseInt(m[1], 10);
    }).unsubscribe();
    this.showEditModal = true;
  }

  closeEdit(): void {
    this.showEditModal = false;
  }

  saveEdit(): void {
    const minutes = Math.max(1, Math.min(240, Number(this.editMinutes || 30)));
    const totalGameSeconds = 15 * 30 * 24 * 60 * 60;
    const ratio = Math.round(totalGameSeconds / (minutes * 60));
    this.accelerator.setTimeCompressionRatio(ratio).subscribe({
      next: () => {
        this.accelerator.restartPolling();
        this.showEditModal = false;
      },
      error: err => {
        console.error('Erreur mise à jour ratio:', err);
        this.showEditModal = false;
      }
    });
  }

  // TimeTravel is handled by `TimeTravelTimelineComponent`
}
