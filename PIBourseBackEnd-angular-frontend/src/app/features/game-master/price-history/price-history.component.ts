import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MarketStockService } from '../../market/services/market-stock.service';
import { ToastService } from '../../../services/toast.service';
import { environment } from '../../../environment/environment';
import { Stock } from '../../market/models/stock.models';

interface PriceHistory {
  id: number;
  stockSymbol: string;
  price: number; // fallback if closePrice not provided
  timestamp: string;
  changePercent: number;
  // Optional OHLC fields if backend provides them
  openPrice?: number;
  closePrice?: number;
  highPrice?: number;
  lowPrice?: number;
  volume?: number;
}

interface PriceUpdate {
  stockSymbol: string;
  newPrice: number;
  reason: string;
}

@Component({
  selector: 'app-price-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './price-history.component.html',
  styleUrls: ['./price-history.component.scss']
})
export class PriceHistoryComponent implements OnInit {
  stocks: Stock[] = [];
  selectedStock: Stock | null = null;
  priceHistory: PriceHistory[] = [];
  filteredHistory: PriceHistory[] = [];
  groupedByMonth: { month: string; days: { date: string; entries: PriceHistory[] }[] }[] = [];
  groupedByDay: { date: string; entries: PriceHistory[] }[] = [];
  loading = false;
  selectedMonth: string = 'all';
  availableMonths: { value: string; label: string }[] = [];
  
  // Formulaire de modification de prix
  priceUpdateForm = {
    newPrice: 0,
    eventType: '',
    eventSymbol: '',
    eventIntensity: 50
  };

  // Market events
  marketEvents = [
    { id: 'CRASH', label: '📉 Crash Boursier', description: 'Ventes massives sur tous les symboles' },
    { id: 'BULL_RUN', label: '📈 Bull Run', description: 'Achats massifs sur tous les symboles' },
    { id: 'VOLATILITY', label: '⚡ Volatilité Extrême', description: 'Ordres aléatoires massifs' },
    { id: 'MANIPULATION', label: '🎯 Manipulation de Prix', description: 'Cibler un symbole spécifique' }
  ];

  constructor(
    private http: HttpClient,
    private stockService: MarketStockService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadStocks();
  }

  loadStocks(): void {
    this.loading = true;
    this.stockService.getAllStocks().subscribe({
      next: (stocks: Stock[]) => {
        this.stocks = stocks;
        if (stocks.length > 0) {
          this.selectStock(stocks[0]);
        }
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des symboles:', err);
        this.toastService.error('Impossible de charger les symboles');
        this.loading = false;
      }
    });
  }

  selectStock(stock: Stock): void {
    this.selectedStock = stock;
    this.priceUpdateForm.newPrice = stock.lastPrice;
    this.loadPriceHistory(stock.symbol);
  }

  loadPriceHistory(stockSymbol: string): void {
    this.loading = true;
    this.http.get<PriceHistory[]>(`${environment.apiUrl}/game-master/price-history/${stockSymbol}`)
      .subscribe({
        next: (history: PriceHistory[]) => {
          this.priceHistory = history;
          // Build month options and default to all
          this.generateAvailableMonths();
          this.selectedMonth = 'all';
          this.filterByMonth();
          this.loading = false;
          console.log('📊 Historique chargé:', history.length, 'entrées');
        },
        error: (err: any) => {
          console.error('Erreur lors du chargement de l\'historique:', err);
          this.toastService.error('Impossible de charger l\'historique des prix');
          this.loading = false;
        }
      });
  }

  groupByMonthAndDay(): void {
    const monthsMap = new Map<string, Map<string, PriceHistory[]>>();
    
    (this.filteredHistory.length ? this.filteredHistory : this.priceHistory).forEach((entry: PriceHistory) => {
      const date = new Date(entry.timestamp);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      const dayKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
      
      if (!monthsMap.has(monthKey)) {
        monthsMap.set(monthKey, new Map<string, PriceHistory[]>());
      }
      const daysMap = monthsMap.get(monthKey)!;
      
      if (!daysMap.has(dayKey)) {
        daysMap.set(dayKey, []);
      }
      daysMap.get(dayKey)!.push(entry);
    });

    this.groupedByMonth = Array.from(monthsMap.entries())
      .sort((a, b) => b[0].localeCompare(a[0])) // Tri décroissant (plus récent d'abord)
      .map(([month, daysMap]) => ({
        month,
        days: Array.from(daysMap.entries())
          .sort((a, b) => b[0].localeCompare(a[0]))
          .map(([date, entries]) => ({
            date,
            entries: entries.sort((a, b) => 
              new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
            )
          }))
      }));

    console.log('📅 Groupé par mois et jour:', this.groupedByMonth);
  }

  // Build available months from payload
  generateAvailableMonths(): void {
    const monthsMap = new Map<string, number>();
    this.priceHistory.forEach((entry: PriceHistory) => {
      const d = new Date(entry.timestamp);
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      monthsMap.set(key, (monthsMap.get(key) || 0) + 1);
    });
    const monthNames = ['Janvier','Février','Mars','Avril','Mai','Juin','Juillet','Août','Septembre','Octobre','Novembre','Décembre'];
    this.availableMonths = [
      { value: 'all', label: `Tous les mois (${this.priceHistory.length})` },
      ...Array.from(monthsMap.entries())
        .sort((a,b)=> b[0].localeCompare(a[0]))
        .map(([key,count])=>{
          const [y,m] = key.split('-');
          return { value: key, label: `${monthNames[parseInt(m)-1]} ${y} (${count})` };
        })
    ];
  }

  // Apply month filter and regroup
  filterByMonth(): void {
    if (this.selectedMonth === 'all') {
      this.filteredHistory = this.priceHistory.slice();
      this.groupByMonthAndDay();
      this.groupedByDay = [];
    } else {
      const [yearStr, monthStr] = this.selectedMonth.split('-');
      const year = parseInt(yearStr, 10);
      const month = parseInt(monthStr, 10);
      this.filteredHistory = this.priceHistory.filter((entry: PriceHistory) => {
        const d = new Date(entry.timestamp);
        return d.getFullYear() === year && (d.getMonth()+1) === month;
      });
      // Build day groups for the selected month view
      const dayMap = new Map<string, PriceHistory[]>();
      this.filteredHistory.forEach((entry: PriceHistory) => {
        const d = new Date(entry.timestamp);
        const key = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
        if (!dayMap.has(key)) dayMap.set(key, []);
        dayMap.get(key)!.push(entry);
      });
      this.groupedByDay = Array.from(dayMap.entries())
        .sort((a,b)=> b[0].localeCompare(a[0]))
        .map(([date, entries])=>({
          date,
          entries: entries.sort((a,b)=> new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
        }));
      // Also keep groupedByMonth limited to the selected month for consistency
      this.groupByMonthAndDay();
    }
  }

  onMonthChange(month: string): void {
    this.selectedMonth = month;
    this.filterByMonth();
  }

  formatDayHeader(dateStr: string): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    const dayNames = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    const monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
                       'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
    
    return `${dayNames[date.getDay()]} ${day} ${monthNames[month - 1]} ${year}`;
  }

  formatMonthHeader(monthStr: string): string {
    const [year, month] = monthStr.split('-').map(Number);
    const monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
                       'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
    return `${monthNames[month - 1]} ${year}`;
  }

  getTotalEntriesForMonth(month: { month: string; days: { date: string; entries: PriceHistory[] }[] }): number {
    return month.days.reduce((total, day) => total + day.entries.length, 0);
  }

  updatePrice(): void {
    if (!this.selectedStock) {
      this.toastService.error('Veuillez sélectionner un symbole');
      return;
    }

    if (this.priceUpdateForm.newPrice <= 0) {
      this.toastService.error('Le prix doit être supérieur à 0');
      return;
    }

    if (this.priceUpdateForm.eventType === 'MANIPULATION' && !this.priceUpdateForm.eventSymbol) {
      this.toastService.error('Veuillez sélectionner un symbole cible pour la manipulation');
      return;
    }

    const update: PriceUpdate = {
      stockSymbol: this.selectedStock.symbol,
      newPrice: this.priceUpdateForm.newPrice,
      reason: this.getEventDescription()
    };

    this.loading = true;
    this.http.post(`${environment.apiUrl}/game-master/update-price`, update)
      .subscribe({
        next: () => {
          this.toastService.success(`Prix de ${this.selectedStock!.symbol} mis à jour avec succès`);
          this.loadPriceHistory(this.selectedStock!.symbol);
          this.loadStocks();
          this.priceUpdateForm.eventType = '';
          this.priceUpdateForm.eventSymbol = '';
        },
        error: (err: any) => {
          console.error('Erreur lors de la mise à jour du prix:', err);
          this.toastService.error('Impossible de mettre à jour le prix');
          this.loading = false;
        }
      });
  }

  triggerMarketEvent(): void {
    if (!this.priceUpdateForm.eventType) {
      this.toastService.error('Veuillez sélectionner un événement');
      return;
    }

    if (this.priceUpdateForm.eventType === 'MANIPULATION' && !this.priceUpdateForm.eventSymbol) {
      this.toastService.error('Veuillez sélectionner un symbole cible');
      return;
    }

    const payload: any = {
      eventType: this.priceUpdateForm.eventType,
      intensity: this.priceUpdateForm.eventIntensity
    };

    if (this.priceUpdateForm.eventType === 'MANIPULATION') {
      payload.symbol = this.priceUpdateForm.eventSymbol;
    }

    this.loading = true;
    // Backend endpoint expects /api/game-master/trigger-event
    this.http.post(`${environment.apiUrl}/game-master/trigger-event`, payload)
      .subscribe({
        next: () => {
          this.toastService.success('Événement de marché déclenché avec succès');
          if (this.selectedStock) {
            this.loadPriceHistory(this.selectedStock.symbol);
          }
          this.loadStocks();
        },
        error: (err: any) => {
          console.error('Erreur lors du déclenchement de l\'événement:', err);
          this.toastService.error('Impossible de déclencher l\'événement');
          this.loading = false;
        }
      });
  }

  getEventDescription(): string {
    const event = this.marketEvents.find(e => e.id === this.priceUpdateForm.eventType);
    return event ? event.label : 'Modification manuelle';
  }

  getSelectedEventDescription(): string {
    const event = this.marketEvents.find(e => e.id === this.priceUpdateForm.eventType);
    return event ? event.description : '';
  }

  getChangeClass(change: number): string {
    if (change > 0) return 'text-emerald-600';
    if (change < 0) return 'text-red-600';
    return 'text-gray-600';
  }

  getChangeIcon(change: number): string {
    if (change > 0) return '↑';
    if (change < 0) return '↓';
    return '→';
  }

  formatDate(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  calculateChange(oldPrice: number, newPrice: number): number {
    return ((newPrice - oldPrice) / oldPrice) * 100;
  }
}
