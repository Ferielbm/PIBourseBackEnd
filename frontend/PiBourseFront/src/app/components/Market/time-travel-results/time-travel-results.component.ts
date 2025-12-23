import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeTravelResult } from '../../../services/Market/time-travel.interface';

@Component({
  selector: 'app-time-travel-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 bg-black/70 flex items-center justify-center p-4 z-50" (click)="close.emit()">
      <div class="bg-gray-900 rounded-xl border border-purple-500/30 w-full max-w-2xl max-h-[90vh] overflow-y-auto" (click)="$event.stopPropagation()">
        <!-- Header -->
        <div class="p-5 border-b border-purple-500/20 bg-gradient-to-r from-purple-900/30 to-pink-900/30">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-bold text-white">Résultats du Voyage Temporel</h3>
            <button (click)="close.emit()" class="text-gray-400 hover:text-white text-2xl">&times;</button>
          </div>
          <p class="text-sm text-purple-300 mt-1">Analyse de performance</p>
        </div>
        
        <!-- Content -->
        <div class="p-5 space-y-6">
          <!-- Performance Summary -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="bg-gray-800/50 p-4 rounded-lg border border-blue-500/20">
              <div class="text-xs text-gray-400">Performance Originale</div>
              <div class="text-2xl font-bold text-blue-400">{{results.originalPerformance}}%</div>
            </div>
            <div class="bg-gray-800/50 p-4 rounded-lg border border-purple-500/20">
              <div class="text-xs text-gray-400">Performance Alternative</div>
              <div class="text-2xl font-bold text-purple-400">{{results.alternativePerformance}}%</div>
            </div>
            <div class="bg-gray-800/50 p-4 rounded-lg border border-emerald-500/20">
              <div class="text-xs text-gray-400">Écart de Performance</div>
              <div class="text-2xl font-bold" [class.text-emerald-400]="results.performanceGap >= 0" [class.text-red-400]="results.performanceGap < 0">
                {{results.performanceGap >= 0 ? '+' : ''}}{{results.performanceGap}}%
              </div>
            </div>
          </div>
          
          <!-- Risk Assessment -->
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
              <div class="text-xs text-gray-400">Niveau de Risque</div>
              <div class="font-bold" [class.text-red-400]="results.riskAssessment === 'HIGH'" [class.text-yellow-400]="results.riskAssessment === 'MEDIUM'" [class.text-emerald-400]="results.riskAssessment === 'LOW'">
                {{getRiskLevel()}}
              </div>
            </div>
            <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
              <div class="text-xs text-gray-400">Ratio de Sharpe</div>
              <div class="font-bold text-cyan-400">{{results.alternativeSharpeRatio}}</div>
            </div>
            <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
              <div class="text-xs text-gray-400">Amélioration Drawdown</div>
              <div class="font-bold text-purple-400">{{results.maxDrawdownImprovement}}%</div>
            </div>
          </div>
        </div>
        
        <!-- Footer -->
        <div class="p-4 border-t border-gray-800 flex justify-end">
          <button (click)="close.emit()" class="px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-sm font-medium transition-colors">
            Fermer
          </button>
        </div>
      </div>
    </div>
  `
})
export class TimeTravelResultsComponent {
  @Input() results!: TimeTravelResult;
  @Output() close = new EventEmitter<void>();
  
  getRiskLevel(): string {
    switch(this.results.riskAssessment) {
      case 'HIGH': return 'Élevé';
      case 'MEDIUM': return 'Moyen';
      case 'LOW': return 'Faible';
      default: return this.results.riskAssessment;
    }
  }
}
