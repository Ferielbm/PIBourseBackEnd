import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DepthChartComponent } from '../../../components/Market/depth-chart/depth-chart.component';

import { Order } from '../../../models/Order/order.models';
import { ScheduledOrderService } from '../../../services/Order/scheduled-order.service';
import { OrderService } from '../../../services/Order/order.service';
import { AuthService } from '../../../services/auth.service';

type SideFilter = 'ALL' | 'BUY' | 'SELL';
type StatusFilter = 'ALL' | 'FILLED' | 'PARTIALLY_FILLED' | 'CANCELLED';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, DepthChartComponent],
  templateUrl: './order.component.html',
  styleUrls: ['./order.component.css'],
})
export class OrdersComponent implements OnInit {
  playerId = 0;

  orders: Order[] = [];
  loading = false;
  error?: string;
  scheduledOrdersCount = 0;
  // message de debug pour diagnostiquer l'absence d'ordres
  debugMessage?: string;

  // 🔹 filtres + recherche
  sideFilter: SideFilter = 'ALL';
  statusFilter: StatusFilter = 'ALL';
  searchTerm = '';
  selectedSymbol: string | null = null;
  // visibilité des statistiques
  showStats = true;

  constructor(
    private orderService: OrderService,
    private router: Router,
    private scheduledOrderService: ScheduledOrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.playerId = this.authService.getCurrentPlayerId() || 0;
    this.orderService.cacheStocks().subscribe({
      next: () => {
        this.loadOrders();
      },
      error: (err) => {
        console.warn('Erreur cache stocks, chargement des ordres quand même:', err);
        this.loadOrders();
      }
    });
    
    this.loadScheduledOrdersCount();
  }

  loadScheduledOrdersCount(): void {
    this.scheduledOrderService.getScheduledOrders(this.playerId).subscribe({
      next: (orders) => {
        this.scheduledOrdersCount = orders.length;
      },
      error: () => {
        this.scheduledOrdersCount = 0;
      }
    });
  }

  goToScheduledOrders(): void {
    this.router.navigate(['/scheduled-orders']);
  }

  loadOrders(): void {
    this.loading = true;
    this.error = undefined;
    this.debugMessage = undefined;

    this.orderService.getAllOrders().subscribe({
      next: (data) => {
        this.orders = data;
        console.log('Orders loaded:', data);
        if (!data || data.length === 0) {
          this.debugMessage = "Aucun ordre renvoyé par l'API (/allorders). Vérifiez le backend.";
        }
        // initialiser selectedSymbol après le chargement des ordres pour éviter
        // de muter l'état pendant le cycle de détection des changements
        if (!this.selectedSymbol) {
          const symbols = this.availableSymbols;
          if (symbols.length > 0) {
            this.selectedSymbol = symbols[0];
          }
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur HTTP lors du chargement des ordres:', err);
        this.error = `Erreur ${err.status || 0} : ${err.statusText || 'inconnue'}`;
        this.loading = false;
      },
    });
  }

  retryLoadOrders(): void {
    this.loadOrders();
  }

  toggleStats(): void {
    this.showStats = !this.showStats;
  }

  /* ---------- FILTRES + RECHERCHE ---------- */

  onSideFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as SideFilter;
    this.sideFilter = value;
  }

  onStatusFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as StatusFilter;
    this.statusFilter = value;
  }

  get filteredOrders(): Order[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.orders.filter(order => {
      const side = String(order.side);
      const st = String(order.status);
      const symbol = this.getSymbolDisplay(order).toLowerCase();

      // filtre type
      if (this.sideFilter !== 'ALL' && side !== this.sideFilter) {
        return false;
      }

      // filtre statut
      if (this.statusFilter !== 'ALL') {
        if (this.statusFilter === 'FILLED' && st !== 'FILLED') return false;
        if (this.statusFilter === 'PARTIALLY_FILLED' && st !== 'PARTIALLY_FILLED') return false;
        if (this.statusFilter === 'CANCELLED') {
          if (st !== 'CANCELLED' && st !== 'CANCELED' && st !== 'REJECTED') return false;
        }
      }

      // filtre recherche par symbole
      if (term && !symbol.includes(term)) {
        return false;
      }

      return true;
    });
  }

  get myOrders(): Order[] {
    // Return player's orders, and if a symbol is selected filter by it as well
    return this.filteredOrders.filter(o => {
      if (o.playerId !== this.playerId) return false;
      if (this.selectedSymbol && this.getSymbolDisplay(o) !== this.selectedSymbol) return false;
      return true;
    });
  }

  // Ordres filtrés pour le symbole sélectionné (order book par symbole)
  get availableSymbols(): string[] {
    const set = new Set<string>();
    for (const o of this.filteredOrders) {
      const sym = this.getSymbolDisplay(o);
      if (sym && sym !== 'N/A') {
        set.add(sym);
      }
    }
    return Array.from(set).sort();
  }

  get ordersForCurrentSymbol(): Order[] {
    const base = this.filteredOrders;
    // Ne pas muter `selectedSymbol` ici : utiliser une valeur de repli locale
    if (!this.selectedSymbol) {
      if (base.length === 0) return [];
      const fallback = this.getSymbolDisplay(base[0]);
      return base.filter(o => this.getSymbolDisplay(o) === fallback);
    }
    return base.filter(o => this.getSymbolDisplay(o) === this.selectedSymbol);
  }

  // Ordres non complètement exécutés pour affichage dans le carnet (PENDING/OPEN/PARTIALLY_FILLED)
  get openOrdersForCurrentSymbol(): Order[] {
    return this.ordersForCurrentSymbol.filter(o => {
      const st = String(o.status || '');
      return st === 'PENDING' || st === 'OPEN' || st === 'PARTIALLY_FILLED';
    });
  }

  /* ---------- LABELS & STYLES ---------- */

  getSideLabel(side: Order['side']): string {
    return side === 'BUY' ? 'Achat' : 'Vente';
  }

  getSideBadgeClass(side: Order['side']): string {
    return side === 'BUY'
      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300'
      : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300';
  }

  getStatusLabel(status: Order['status'] | string): string {
    const st = String(status);
    switch (st) {
      case 'FILLED': return 'Exécuté';
      case 'PARTIALLY_FILLED': return 'Partiel';
      case 'CANCELLED':
      case 'CANCELED':
      case 'REJECTED': return 'Annulé';
      case 'PENDING':
      case 'OPEN': return 'Ouvert';
      default: return st;
    }
  }

  getStatusBadgeClass(status: Order['status'] | string): string {
    const st = String(status);
    switch (st) {
      case 'FILLED':
        return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300';
      case 'PARTIALLY_FILLED':
        return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300';
      case 'CANCELLED':
      case 'CANCELED':
      case 'REJECTED':
        return 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300';
      case 'OPEN':
      case 'PENDING':
      default:
        return 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300';
    }
  }

  getUnitPrice(order: Order): number {
    return order.executedPrice ?? order.price ?? 0;
  }

  getAmount(order: Order): number {
    return this.getUnitPrice(order) * order.quantity;
  }

  getSymbolDisplay(order: Order): string {
    if (order.symbol && order.symbol.trim()) {
      return order.symbol;
    }
    if (order.stockId) {
      const cachedSymbol = this.orderService.getSymbolFromCache(order.stockId);
      if (cachedSymbol) return cachedSymbol;
      return `Stock #${order.stockId}`;
    }
    return 'N/A';
  }

  cancelMyOrder(order: Order): void {
    if (!order.id) return;
    if (!window.confirm(`Annuler l'ordre #${order.id} ?`)) return;

    this.orderService.cancelOrder(this.playerId, order.id).subscribe({
      next: () => this.loadOrders(),
      error: (err) => {
        console.error('Erreur annulation ordre:', err);
        window.alert("Erreur lors de l'annulation de l'ordre.");
      }
    });
  }

  /* ---------- GROUPING FOR ORDER-BOOK STYLE ---------- */

  private groupOrdersByPrice(orders: Order[]): { price: number; count: number; volume: number }[] {
    const map = new Map<number, { count: number; volume: number }>();
    for (const o of orders) {
      const price = this.getUnitPrice(o);
      const cur = map.get(price) || { count: 0, volume: 0 };
      cur.count += 1;
      cur.volume += o.quantity;
      map.set(price, cur);
    }
    const rows = Array.from(map.entries()).map(([price, v]) => ({ price, count: v.count, volume: v.volume }));
    return rows;
  }

  get groupedBuyRows(): { price: number; count: number; volume: number }[] {
    const buys = this.openOrdersForCurrentSymbol.filter(o => o.side === 'BUY');
    return this.groupOrdersByPrice(buys).sort((a, b) => b.price - a.price);
  }

  get groupedSellRows(): { price: number; count: number; volume: number }[] {
    const sells = this.openOrdersForCurrentSymbol.filter(o => o.side === 'SELL');
    return this.groupOrdersByPrice(sells).sort((a, b) => a.price - b.price);
  }

  // ---------- BEST BID / ASK & SPREAD ----------

  get bestBid(): number | null {
    return this.groupedBuyRows.length > 0 ? this.groupedBuyRows[0].price : null;
  }

  get bestAsk(): number | null {
    return this.groupedSellRows.length > 0 ? this.groupedSellRows[0].price : null;
  }

  get midPrice(): number | null {
    if (this.bestBid == null || this.bestAsk == null) return null;
    return (this.bestBid + this.bestAsk) / 2;
  }

  get spread(): number | null {
    if (this.bestBid == null || this.bestAsk == null) return null;
    return this.bestAsk - this.bestBid;
  }

  // ---------- DEPTH (CUMUL DES VOLUMES) ----------

  get buyDepthLevels(): { price: number; count: number; volume: number; cumulativeVolume: number }[] {
    let cum = 0;
    return this.groupedBuyRows.map(level => {
      cum += level.volume;
      return { ...level, cumulativeVolume: cum };
    });
  }

  get sellDepthLevels(): { price: number; count: number; volume: number; cumulativeVolume: number }[] {
    let cum = 0;
    return this.groupedSellRows.map(level => {
      cum += level.volume;
      return { ...level, cumulativeVolume: cum };
    });
  }

  get maxDepthVolume(): number {
    const lastBuy = this.buyDepthLevels.length > 0 ? this.buyDepthLevels[this.buyDepthLevels.length - 1].cumulativeVolume : 0;
    const lastSell = this.sellDepthLevels.length > 0 ? this.sellDepthLevels[this.sellDepthLevels.length - 1].cumulativeVolume : 0;
    const max = Math.max(lastBuy, lastSell);
    return max > 0 ? max : 1;
  }

  get totalBuyVolume(): number {
    return this.openOrdersForCurrentSymbol.filter(o => o.side === 'BUY').reduce((s, o) => s + o.quantity, 0);
  }

  get totalSellVolume(): number {
    return this.openOrdersForCurrentSymbol.filter(o => o.side === 'SELL').reduce((s, o) => s + o.quantity, 0);
  }

  get buyPercent(): number {
    const total = this.totalBuyVolume + this.totalSellVolume;
    return total > 0 ? Math.round((this.totalBuyVolume / total) * 100) : 0;
  }

  get sellPercent(): number {
    const total = this.totalBuyVolume + this.totalSellVolume;
    return total > 0 ? 100 - this.buyPercent : 0;
  }
}
