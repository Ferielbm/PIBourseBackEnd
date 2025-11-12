import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeAcceleratorService } from '../../../services/Market/time-accelerator.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-time-control',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="rounded-xl border border-emerald-500/30 bg-gradient-to-br from-emerald-900/20 to-cyan-900/20 p-4">
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2">
          <div class="text-2xl">⏱️</div>
          <div>
            <div class="font-bold text-gray-100">Accélérateur Temporel</div>
            <div class="text-xs text-gray-400">12 mois simulés en 30 minutes</div>
          </div>
        </div>
        <div *ngIf="isActive" class="flex items-center gap-2 px-3 py-1.5 bg-emerald-500/20 rounded-full border border-emerald-500/30">
          <div class="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></div>
          <span class="text-xs font-medium text-emerald-400">ACTIF</span>
        </div>
      </div>
      
      <!-- Date et heure actuelles -->
      <div class="mb-4 p-3 bg-black/30 rounded-lg border border-gray-700/50">
        <div class="text-xs text-gray-400 mb-1">Date de simulation</div>
        <div class="text-2xl font-bold text-white font-mono">
          {{currentGameTime | date:'dd/MM/yyyy HH:mm:ss'}}
        </div>
        <div class="text-xs text-cyan-400 mt-1">
          🚀 Vitesse: 4032x (1 sec réelle = 67.2 min simulées)
        </div>
      </div>
      
      <!-- Boutons de contrôle -->
      <div class="grid grid-cols-3 gap-2">
        <button 
          (click)="start()"
          [disabled]="isActive"
          [class.opacity-50]="isActive"
          [class.cursor-not-allowed]="isActive"
          class="px-3 py-2.5 bg-gradient-to-r from-emerald-600 to-cyan-600 hover:from-emerald-500 hover:to-cyan-500 text-white font-semibold rounded-lg transition-all shadow-lg hover:shadow-emerald-500/50 disabled:hover:shadow-none text-sm">
          ▶️ Démarrer
        </button>
        
        <button 
          (click)="stop()"
          [disabled]="!isActive"
          [class.opacity-50]="!isActive"
          [class.cursor-not-allowed]="!isActive"
          class="px-3 py-2.5 bg-gradient-to-r from-red-600 to-pink-600 hover:from-red-500 hover:to-pink-500 text-white font-semibold rounded-lg transition-all shadow-lg hover:shadow-red-500/50 disabled:hover:shadow-none text-sm">
          ⏸️ Pause
        </button>
        
        <button 
          (click)="reset()"
          [disabled]="isActive"
          [class.opacity-50]="isActive"
          [class.cursor-not-allowed]="isActive"
          class="px-3 py-2.5 bg-gradient-to-r from-gray-600 to-gray-700 hover:from-gray-500 hover:to-gray-600 text-white font-semibold rounded-lg transition-all shadow-lg hover:shadow-gray-500/50 disabled:hover:shadow-none text-sm">
          🔄 Reset
        </button>
      </div>
      
      <!-- Progression de l'année -->
      <div class="mt-4">
        <div class="flex items-center justify-between text-xs text-gray-400 mb-1">
          <span>Progression 2023</span>
          <span>{{yearProgress}}%</span>
        </div>
        <div class="h-2 bg-gray-800 rounded-full overflow-hidden">
          <div 
            class="h-full bg-gradient-to-r from-emerald-500 to-cyan-500 transition-all duration-1000"
            [style.width.%]="yearProgress">
          </div>
        </div>
      </div>
      
      <!-- Message d'erreur -->
      <div *ngIf="errorMessage" class="mt-3 p-2 bg-red-500/10 border border-red-500/30 rounded text-xs text-red-400">
        ⚠️ {{errorMessage}}
      </div>
    </div>
  `,
  styles: [`
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }
    .animate-pulse {
      animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
    }
  `]
})
export class TimeControlComponent implements OnInit, OnDestroy {
  currentGameTime: Date | null = null;
  isActive = false;
  yearProgress = 0;
  errorMessage = '';
  
  private timeSubscription?: Subscription;
  private activeSubscription?: Subscription;

  constructor(private timeAccelerator: TimeAcceleratorService) {}

  ngOnInit() {
    // S'abonner aux changements de temps
    this.timeSubscription = this.timeAccelerator.getCurrentGameTime().subscribe(time => {
      this.currentGameTime = time;
      if (time) {
        this.calculateYearProgress(time);
      }
    });

    // S'abonner à l'état actif
    this.activeSubscription = this.timeAccelerator.isAccelerationActive().subscribe(active => {
      this.isActive = active;
    });
  }

  ngOnDestroy() {
    this.timeSubscription?.unsubscribe();
    this.activeSubscription?.unsubscribe();
  }

  start() {
    this.errorMessage = '';
    this.timeAccelerator.startAcceleration().subscribe({
      next: () => {
        console.log('✅ Time acceleration started');
      },
      error: (err) => {
        this.errorMessage = 'Erreur: Backend non disponible. Vérifiez que Spring Boot est démarré.';
        console.error('❌ Failed to start acceleration:', err);
      }
    });
  }

  stop() {
    this.errorMessage = '';
    this.timeAccelerator.stopAcceleration().subscribe({
      next: () => {
        console.log('⏸️ Time acceleration stopped');
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors de l\'arrêt';
        console.error('❌ Failed to stop acceleration:', err);
      }
    });
  }

  reset() {
    this.errorMessage = '';
    this.timeAccelerator.resetAcceleration().subscribe({
      next: () => {
        console.log('🔄 Time acceleration reset to start');
        this.yearProgress = 0;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors de la réinitialisation';
        console.error('❌ Failed to reset acceleration:', err);
      }
    });
  }

  private calculateYearProgress(date: Date) {
    const yearStart = new Date(2023, 0, 1).getTime();
    const yearEnd = new Date(2023, 11, 31, 23, 59, 59).getTime();
    const current = date.getTime();
    
    this.yearProgress = Math.min(100, Math.max(0, 
      ((current - yearStart) / (yearEnd - yearStart)) * 100
    ));
  }
}
