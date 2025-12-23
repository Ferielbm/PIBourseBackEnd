import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatsBarComponent } from './stats-bar/stats-bar.component';
import { OrderBookService, OrderBookSnapshot } from '../../../services/Order/order-book.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-order-book-display',
  standalone: true,
  imports: [CommonModule, StatsBarComponent],
  templateUrl: './order-book-display.component.html',
  styleUrls: ['./order-book-display.component.css']
})
export class OrderBookDisplayComponent implements OnInit, OnDestroy {
  @Input() symbol: string = 'AAPL';
  @Input() autoRefresh: boolean = true;
  @Input() refreshIntervalMs: number = 3000;
  @Input() showStatsDefault: boolean = true;
  
  orderBook: OrderBookSnapshot | null = null;
  isLoading = false;
  private refreshSubscription?: Subscription;
  showStats = true;
  
  // Debug
  totalOrdersFromApi = 0;
  filteredOrdersCount = 0;
  lastLoadTime = '';
  
  constructor(private orderBookService: OrderBookService) {}
  
  ngOnInit(): void {
    this.showStats = this.showStatsDefault;
    this.loadOrderBook();
    
    if (this.autoRefresh) {
      this.startAutoRefresh();
    }
  }
  
  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
  }
  
  /**
   * 📊 Charge le carnet d'ordres complet depuis la base de données
   * Affiche TOUS les ordres de TOUS les joueurs pour ce symbole
   */
  loadOrderBook(): void {
    if (!this.symbol) return;
    
    console.log(`📖 [OrderBook] Chargement pour ${this.symbol}...`);
    this.isLoading = true;
    this.lastLoadTime = new Date().toLocaleTimeString();
    
    this.orderBookService.getOrderBookFromDatabase(this.symbol).subscribe({
      next: (snapshot: OrderBookSnapshot) => {
        this.orderBook = snapshot;
        this.isLoading = false;
        
        const bidCount = Object.keys(snapshot.bids).length;
        const askCount = Object.keys(snapshot.asks).length;
        const totalBidQty = Object.values(snapshot.bids).reduce((sum: number, qty: number) => sum + qty, 0);
        const totalAskQty = Object.values(snapshot.asks).reduce((sum: number, qty: number) => sum + qty, 0);
        
        console.log(`✅ [OrderBook] Carnet chargé: ${bidCount} niveaux achat (${totalBidQty} actions), ${askCount} niveaux vente (${totalAskQty} actions)`);
        console.log(`📊 [OrderBook] Snapshot complet:`, snapshot);
      },
      error: (err: any) => {
        console.error('❌ [OrderBook] Erreur chargement:', err);
        this.isLoading = false;
      }
    });
  }
  
  /**
   * 🔄 Démarre le rafraîchissement automatique
   */
  private startAutoRefresh(): void {
    this.refreshSubscription = interval(this.refreshIntervalMs)
      .pipe(
        switchMap(() => {
          console.log(`🔄 [OrderBook] Auto-refresh pour ${this.symbol}`);
          return this.orderBookService.getOrderBookFromDatabase(this.symbol);
        })
      )
      .subscribe({
        next: (snapshot: OrderBookSnapshot) => {
          this.orderBook = snapshot;
          const bidCount = Object.keys(snapshot.bids).length;
          const askCount = Object.keys(snapshot.asks).length;
          console.log(`✅ [OrderBook] Auto-refresh: ${bidCount} bids, ${askCount} asks`);
        },
        error: (err: any) => {
          console.error('❌ [OrderBook] Erreur auto-refresh:', err);
        }
      });
  }
  
  /**
   * 🔢 Récupère les prix du carnet triés
   */
  getBidPrices(): number[] {
    if (!this.orderBook) return [];
    return Object.keys(this.orderBook.bids)
      .map(p => parseFloat(p))
      .sort((a, b) => b - a); // décroissant (meilleur prix d'achat en haut)
  }
  
  getAskPrices(): number[] {
    if (!this.orderBook) return [];
    return Object.keys(this.orderBook.asks)
      .map(p => parseFloat(p))
      .sort((a, b) => a - b); // croissant (meilleur prix de vente en haut)
  }
  
  /**
   * 📈 Récupère la quantité totale pour un niveau de prix
   */
  getBidQuantity(price: number): number {
    return this.orderBook?.bids[price] || 0;
  }
  
  getAskQuantity(price: number): number {
    return this.orderBook?.asks[price] || 0;
  }
  
  /**
   * 🎨 Calcule la largeur de la barre de profondeur (pour visualisation)
   */
  getDepthBarWidth(quantity: number, side: 'bid' | 'ask'): number {
    if (!this.orderBook) return 0;
    
    const allQuantities = side === 'bid' 
      ? Object.values(this.orderBook.bids)
      : Object.values(this.orderBook.asks);
    
    const maxQuantity = Math.max(...(allQuantities as number[]), 1);
    return (quantity / maxQuantity) * 100;
  }
  
  /**
   * 📊 Calcule le total des quantités pour les achats
   */
  getTotalBids(): number {
    if (!this.orderBook) return 0;
    return this.getBidPrices().reduce((sum, price) => sum + this.getBidQuantity(price), 0);
  }
  
  /**
   * 📊 Calcule le total des quantités pour les ventes
   */
  getTotalAsks(): number {
    if (!this.orderBook) return 0;
    return this.getAskPrices().reduce((sum, price) => sum + this.getAskQuantity(price), 0);
  }
  
  toggleStats(): void {
    this.showStats = !this.showStats;
  }
}
