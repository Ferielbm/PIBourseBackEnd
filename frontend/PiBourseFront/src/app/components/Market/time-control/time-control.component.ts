import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeAcceleratorService } from '../../../services/Market/time-accelerator.service';
import { SimulationTimeService } from '../../../services/Market/simulation-time.service';
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
            <div class="text-xs text-gray-400">Fenêtre Oct 2022 → Déc 2023 (15 mois)</div>
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
          🚀 Compression dynamique – fenêtre de 15 mois
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
          <span>{{progressLabel}}</span>
          <span>{{simulationProgress | number:'1.0-1'}}%</span>
        </div>
        <div class="h-2 bg-gray-800 rounded-full overflow-hidden">
          <div 
            class="h-full bg-gradient-to-r from-emerald-500 to-cyan-500 transition-all duration-1000"
            [style.width.%]="simulationProgress">
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
  simulationProgress = 0;
  errorMessage = '';
  progressLabel: string;
  private readonly simulationBounds;
  
  private timeSubscription?: Subscription;
  private activeSubscription?: Subscription;

  constructor(
    private timeAccelerator: TimeAcceleratorService,
    private simulationTime: SimulationTimeService
  ) {
    this.simulationBounds = this.simulationTime.getSimulationBounds();
    this.progressLabel = `Progression ${this.simulationBounds.start.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' })} → ${this.simulationBounds.end.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' })}`;
    // Forcer l'utilisation du temps local pour commencer au 01/01/2023
    this.currentGameTime = this.simulationTime.getCurrentSimDate();
  }

  ngOnInit() {
    // Utiliser le service local en premier pour garantir le bon point de départ
    this.currentGameTime = this.simulationTime.getCurrentSimDate();
    this.calculateSimulationProgress(this.currentGameTime);
    
    // S'abonner au temps local (prioritaire)
    this.timeSubscription = this.simulationTime.currentSimDateTime$.subscribe(time => {
      this.currentGameTime = time;
      this.calculateSimulationProgress(time);
    });

    // Essayer de s'abonner au backend en fallback (si disponible)
    this.activeSubscription = this.timeAccelerator.isAccelerationActive().subscribe({
      next: (active) => {
        this.isActive = active;
      },
      error: () => {
        // Ignorer les erreurs du backend, utiliser le service local
        console.log('Backend non disponible, utilisation du service local');
        this.isActive = false;
      }
    });
  }

  ngOnDestroy() {
    this.timeSubscription?.unsubscribe();
    this.activeSubscription?.unsubscribe();
  }

  start() {
    this.errorMessage = '';
    this.isActive = true;
    
    // Utiliser le service local directement
    this.simulationTime.startSimulation();
    console.log('✅ Time acceleration started (local service)');
    
    // Essayer le backend en arrière-plan (sans bloquer)
    this.timeAccelerator.startAcceleration().subscribe({
      next: () => {
        console.log('✅ Backend acceleration also started');
      },
      error: (err) => {
        console.log('ℹ️ Backend non disponible, utilisation du service local uniquement');
      }
    });
  }

  stop() {
    this.errorMessage = '';
    this.isActive = false;
    
    // Utiliser le service local directement
    this.simulationTime.stopSimulation();
    console.log('⏸️ Time acceleration stopped (local service)');
    
    // Essayer le backend en arrière-plan
    this.timeAccelerator.stopAcceleration().subscribe({
      error: (err) => {
        console.log('ℹ️ Backend non disponible pour l\'arrêt');
      }
    });
  }

  reset() {
    this.errorMessage = '';
    this.isActive = false;
    
    // Utiliser le service local directement
    this.simulationTime.resetSimulation();
    this.simulationProgress = 0;
    this.currentGameTime = this.simulationTime.getCurrentSimDate();
    console.log('🔄 Time acceleration reset to start (local service)');
    
    // Essayer le backend en arrière-plan
    this.timeAccelerator.resetAcceleration().subscribe({
      error: (err) => {
        console.log('ℹ️ Backend non disponible pour la réinitialisation');
      }
    });
  }

  private calculateSimulationProgress(date: Date) {
    const start = this.simulationBounds.start.getTime();
    const end = this.simulationBounds.end.getTime();
    const current = Math.min(Math.max(date.getTime(), start), end);

    this.simulationProgress = Math.min(100, Math.max(0,
      ((current - start) / (end - start)) * 100
    ));
  }
}
