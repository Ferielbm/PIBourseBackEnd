import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketStatisticsService, StockPerformance, MarketOverview } from '../../../services/Market/market-statistics.service';

@Component({
  selector: 'app-market-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="rounded-xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
      <div class="flex items-center gap-2 mb-4">
        <div class="text-xl">📊</div>
        <div class="font-semibold text-gray-900 dark:text-gray-100">Analyse de Marché</div>
      </div>
      
      <!-- Sélecteur de type d'analyse -->
      <div class="mb-3">
        <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
          Type d'analyse
        </label>
        <select 
          [(ngModel)]="analysisType" 
          (change)="onTypeChange()"
          class="w-full px-3 py-2 bg-gray-50 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg text-sm focus:ring-2 focus:ring-cyan-500 focus:border-transparent">
          <option value="stock">Performance d'une action</option>
          <option value="top">Top Performers</option>
          <option value="overview">Aperçu du marché</option>
        </select>
      </div>
      
      <!-- Formulaire selon le type -->
      <div class="space-y-3 mb-4">
        <!-- Symbole pour performance individuelle -->
        <div *ngIf="analysisType === 'stock'">
          <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Symbole
          </label>
          <select 
            [(ngModel)]="symbol"
            class="w-full px-3 py-2 bg-gray-50 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg text-sm focus:ring-2 focus:ring-cyan-500 focus:border-transparent">
            <option *ngFor="let s of availableSymbols" [value]="s">{{s}}</option>
          </select>
        </div>
        
        <!-- Limite pour top performers -->
        <div *ngIf="analysisType === 'top'">
          <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Nombre de résultats
          </label>
          <input 
            type="number" 
            [(ngModel)]="limit"
            min="1"
            max="50"
            class="w-full px-3 py-2 bg-gray-50 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg text-sm focus:ring-2 focus:ring-cyan-500 focus:border-transparent">
        </div>
      </div>
      
      <!-- Bouton Générer -->
      <button 
        (click)="generateAnalysis()"
        [disabled]="loading"
        class="w-full px-4 py-2.5 bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 disabled:from-gray-600 disabled:to-gray-700 text-white font-semibold rounded-lg transition-all shadow-lg hover:shadow-cyan-500/50 disabled:opacity-50 disabled:cursor-not-allowed">
        <span *ngIf="!loading">📈 Générer l'analyse</span>
        <span *ngIf="loading">⏳ Chargement...</span>
      </button>
      
      <!-- Message d'erreur -->
      <div *ngIf="error" class="mt-3 p-2 bg-red-500/10 border border-red-500/30 rounded text-xs text-red-400">
        ⚠️ {{error}}
      </div>
    </div>
    
    <!-- Modal Popup -->
    <div *ngIf="showModal" 
         (click)="closeModal()"
         class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fade-in">
      <div (click)="$event.stopPropagation()" 
           class="bg-gray-900 rounded-2xl border border-gray-700 shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden animate-scale-in">
        
        <!-- Header -->
        <div class="bg-gradient-to-r from-cyan-900/50 to-blue-900/50 p-4 border-b border-gray-700 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="text-2xl">📊</div>
            <div>
              <div class="font-bold text-white text-lg">Analyse de Marché</div>
              <div class="text-xs text-gray-400">Documentation pour le trader</div>
            </div>
          </div>
          <button 
            (click)="closeModal()"
            class="text-gray-400 hover:text-white transition-colors text-2xl leading-none">
            ×
          </button>
        </div>
        
        <!-- Content -->
        <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
          <!-- Performance d'une action -->
          <div *ngIf="analysisType === 'stock' && stockPerformance">
            <div class="grid grid-cols-2 gap-4 mb-6">
              <div class="bg-gradient-to-br from-emerald-900/20 to-cyan-900/20 p-4 rounded-lg border border-emerald-500/30">
                <div class="text-xs text-gray-400 mb-1">Symbole</div>
                <div class="text-2xl font-bold text-white">{{stockPerformance.symbol}}</div>
              </div>
              <div class="bg-gradient-to-br from-blue-900/20 to-purple-900/20 p-4 rounded-lg border border-blue-500/30">
                <div class="text-xs text-gray-400 mb-1">Rendement Total</div>
                <div class="text-2xl font-bold" [class.text-emerald-400]="getReturnValue(stockPerformance.totalReturn) >= 0" [class.text-red-400]="getReturnValue(stockPerformance.totalReturn) < 0">
                  {{stockPerformance.totalReturn}}
                </div>
              </div>
            </div>
            
            <div class="grid grid-cols-3 gap-3 mb-6">
              <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
                <div class="text-xs text-gray-400 mb-1">Prix Début</div>
                <div class="text-lg font-semibold text-white">\${{stockPerformance.startPrice}}</div>
              </div>
              <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
                <div class="text-xs text-gray-400 mb-1">Prix Fin</div>
                <div class="text-lg font-semibold text-white">\${{stockPerformance.endPrice}}</div>
              </div>
              <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
                <div class="text-xs text-gray-400 mb-1">Variation</div>
                <div class="text-lg font-semibold" [class.text-emerald-400]="stockPerformance.priceChange >= 0" [class.text-red-400]="stockPerformance.priceChange < 0">
                  {{stockPerformance.priceChange >= 0 ? '+' : ''}}\${{stockPerformance.priceChange}}
                </div>
              </div>
            </div>
            
            <div class="grid grid-cols-3 gap-3 mb-6">
              <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
                <div class="text-xs text-gray-400 mb-1">Volatilité</div>
                <div class="text-lg font-semibold text-orange-400">{{stockPerformance.volatility}}</div>
              </div>
              <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
                <div class="text-xs text-gray-400 mb-1">Jours Analysés</div>
                <div class="text-lg font-semibold text-white">{{stockPerformance.totalDays}}</div>
              </div>
              <div class="bg-gray-800/50 p-3 rounded-lg border border-gray-700">
                <div class="text-xs text-gray-400 mb-1">Volume Moyen</div>
                <div class="text-lg font-semibold text-white">{{formatNumber(stockPerformance.averageVolume)}}</div>
              </div>
            </div>
            
            <!-- Performance mensuelle -->
            <div class="bg-gray-800/30 p-4 rounded-lg border border-gray-700">
              <div class="font-semibold text-white mb-3">📈 Performance Mensuelle</div>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-2">
                <div *ngFor="let month of getMonthlyKeys(stockPerformance.monthlyPerformance)" 
                     class="bg-gray-900/50 p-2 rounded border border-gray-700">
                  <div class="text-xs text-gray-400">{{month}}</div>
                  <div class="text-sm font-semibold" 
                       [class.text-emerald-400]="stockPerformance.monthlyPerformance[month] >= 0"
                       [class.text-red-400]="stockPerformance.monthlyPerformance[month] < 0">
                    {{stockPerformance.monthlyPerformance[month] >= 0 ? '+' : ''}}{{stockPerformance.monthlyPerformance[month]}}%
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Top Performers -->
          <div *ngIf="analysisType === 'top' && topPerformers">
            <div class="space-y-3">
              <div *ngFor="let perf of topPerformers; let i = index" 
                   class="bg-gradient-to-r from-gray-800/50 to-gray-900/50 p-4 rounded-lg border border-gray-700 hover:border-cyan-500/50 transition-all">
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-full bg-gradient-to-br from-cyan-500 to-blue-500 flex items-center justify-center font-bold text-white">
                      {{i + 1}}
                    </div>
                    <div class="text-xl font-bold text-white">{{perf.symbol}}</div>
                  </div>
                  <div class="text-2xl font-bold" [class.text-emerald-400]="getReturnValue(perf.totalReturn) >= 0" [class.text-red-400]="getReturnValue(perf.totalReturn) < 0">
                    {{perf.totalReturn}}
                  </div>
                </div>
                <div class="grid grid-cols-4 gap-2 text-xs">
                  <div>
                    <span class="text-gray-400">Prix: </span>
                    <span class="text-white font-semibold">$ {{perf.endPrice}}</span>
                  </div>
                  <div>
                    <span class="text-gray-400">Change: </span>
                    <span class="font-semibold" [class.text-emerald-400]="perf.priceChange >= 0" [class.text-red-400]="perf.priceChange < 0">
                      {{perf.priceChange >= 0 ? '+' : ''}}$ {{perf.priceChange}}
                    </span>
                  </div>
                  <div>
                    <span class="text-gray-400">Vol: </span>
                    <span class="text-orange-400 font-semibold">{{perf.volatility}}</span>
                  </div>
                  <div>
                    <span class="text-gray-400">Volume: </span>
                    <span class="text-white font-semibold">{{formatNumber(perf.averageVolume)}}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Market Overview -->
          <div *ngIf="analysisType === 'overview' && marketOverview">
            <div class="grid grid-cols-2 gap-4 mb-6">
              <div class="bg-gradient-to-br from-purple-900/20 to-pink-900/20 p-4 rounded-lg border border-purple-500/30">
                <div class="text-xs text-gray-400 mb-1">Actions Totales</div>
                <div class="text-3xl font-bold text-white">{{marketOverview.totalStocks}}</div>
              </div>
              <div class="bg-gradient-to-br from-emerald-900/20 to-teal-900/20 p-4 rounded-lg border border-emerald-500/30">
                <div class="text-xs text-gray-400 mb-1">Capitalisation Totale</div>
                <div class="text-3xl font-bold text-emerald-400">$ {{formatNumber(marketOverview.totalMarketCap)}}</div>
              </div>
            </div>
            
            <div class="bg-gray-800/50 p-4 rounded-lg border border-gray-700 mb-6">
              <div class="text-xs text-gray-400 mb-1">Prix Moyen des Actions</div>
              <div class="text-2xl font-bold text-white">$ {{marketOverview.averageStockPrice}}</div>
            </div>
            
            <!-- Par secteur -->
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-800/30 p-4 rounded-lg border border-gray-700">
                <div class="font-semibold text-white mb-3">📊 Actions par Secteur</div>
                <div class="space-y-2">
                  <div *ngFor="let sector of getSectorKeys(marketOverview.stocksBySector)" 
                       class="flex items-center justify-between text-sm">
                    <span class="text-gray-400">{{sector}}</span>
                    <span class="text-white font-semibold">{{marketOverview.stocksBySector[sector]}}</span>
                  </div>
                </div>
              </div>
              
              <div class="bg-gray-800/30 p-4 rounded-lg border border-gray-700">
                <div class="font-semibold text-white mb-3">💰 Capitalisation par Secteur</div>
                <div class="space-y-2">
                  <div *ngFor="let sector of getSectorKeys(marketOverview.marketCapBySector)" 
                       class="flex items-center justify-between text-sm">
                    <span class="text-gray-400">{{sector}}</span>
                    <span class="text-emerald-400 font-semibold">$ {{formatNumber(marketOverview.marketCapBySector[sector])}}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="mt-4 text-xs text-gray-500 text-center">
              Dernière mise à jour: {{marketOverview.lastUpdate | date:'dd/MM/yyyy HH:mm:ss'}}
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes fade-in {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    
    @keyframes scale-in {
      from { transform: scale(0.95); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }
    
    .animate-fade-in {
      animation: fade-in 0.2s ease-out;
    }
    
    .animate-scale-in {
      animation: scale-in 0.3s ease-out;
    }
  `]
})
export class MarketAnalysisComponent {
  analysisType: 'stock' | 'top' | 'overview' = 'stock';
  symbol = 'AAPL';
  limit = 10;
  loading = false;
  error = '';
  showModal = false;
  
  // Liste des symboles disponibles
  availableSymbols = ['AAPL', 'MSFT', 'NVDA', 'TSLA', 'GOOGL', 'AMZN', 'META'];
  
  stockPerformance: StockPerformance | null = null;
  topPerformers: StockPerformance[] | null = null;
  marketOverview: MarketOverview | null = null;

  constructor(private statsService: MarketStatisticsService) {}

  onTypeChange() {
    this.error = '';
    this.stockPerformance = null;
    this.topPerformers = null;
    this.marketOverview = null;
  }

  generateAnalysis() {
    this.error = '';
    this.loading = true;
    
    if (this.analysisType === 'stock') {
      if (!this.symbol || this.symbol.trim() === '') {
        this.error = 'Veuillez entrer un symbole';
        this.loading = false;
        return;
      }
      
      this.statsService.getStockPerformance(this.symbol.toUpperCase()).subscribe({
        next: (data) => {
          this.stockPerformance = data;
          this.showModal = true;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des données';
          console.error(err);
          this.loading = false;
        }
      });
    } else if (this.analysisType === 'top') {
      this.statsService.getTopPerformers(this.limit).subscribe({
        next: (data) => {
          this.topPerformers = data;
          this.showModal = true;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des données';
          console.error(err);
          this.loading = false;
        }
      });
    } else if (this.analysisType === 'overview') {
      this.statsService.getMarketOverview().subscribe({
        next: (data) => {
          this.marketOverview = data;
          this.showModal = true;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des données';
          console.error(err);
          this.loading = false;
        }
      });
    }
  }

  closeModal() {
    this.showModal = false;
  }

  getReturnValue(returnStr: string): number {
    return parseFloat(returnStr.replace('%', ''));
  }

  formatNumber(num: number): string {
    return new Intl.NumberFormat('fr-FR').format(num);
  }

  getMonthlyKeys(monthly: { [key: string]: number }): string[] {
    return Object.keys(monthly);
  }

  getSectorKeys(sectors: { [key: string]: any }): string[] {
    return Object.keys(sectors);
  }
}
