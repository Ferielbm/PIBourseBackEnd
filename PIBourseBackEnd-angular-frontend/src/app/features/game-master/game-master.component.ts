import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { GameMasterService } from '../../services/game-master.service';
import { OrderBookService } from '../../services/Order/order-book.service';
import { MarketStockService } from '../market/services/market-stock.service';
import { ToastService } from '../../services/toast.service';
import { Stock } from '../market/models/stock.models';
import { Subscription, interval } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';
import { environment } from '../../environment/environment';

interface OrderBookLevel {
  price: string;
  quantity: number;
}

interface OrderBookData {
  symbol: string;
  bids: OrderBookLevel[];
  asks: OrderBookLevel[];
  lastPrice: number;
  spread: number;
}

interface MarketStats {
  totalOrders: number;
  totalVolume: number;
  activePlayers: number;
  mostTradedSymbol: string;
}

interface Order {
  id: number;
  side: string;
  quantity: number;
  price: number;
  status: string;
  stockId: number;
  playerId: number;
  remainingQuantity: number;
  type: string;
  tif: string;
  createdAt: string;
  updatedAt: string;
  stock: { 
    id: number;
    symbol: string; 
    name: string; 
    lastPrice: number;
  };
  player: { 
    id: number;
    username: string;
  };
}

@Component({
  selector: 'app-game-master',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './game-master.component.html',
  styleUrls: ['./game-master.component.css']
})
export class GameMasterComponent implements OnInit, OnDestroy {
  // Données du carnet d'ordres
  symbols: Stock[] = [];
  selectedSymbol: string = '';
  orderBooks: Map<string, OrderBookData> = new Map();
  currentOrderBook: OrderBookData | null = null;
  
  // Liste de TOUS les ordres (non filtrés)
  allOrders: Order[] = [];
  allOrdersLoading = false;

  // Injection d'ordres fictifs
  fictiveOrderForm = {
    symbol: '',
    side: 'BUY' as 'BUY' | 'SELL',
    quantity: 1000,
    priceType: 'MARKET' as 'MARKET' | 'LIMIT',
    limitPrice: 0,
    description: 'Ordre massif du meneur de jeu'
  };

  // Prix du marché actuel
  currentMarketPrice: number = 0;

  // Statistiques
  stats: MarketStats = {
    totalOrders: 0,
    totalVolume: 0,
    activePlayers: 0,
    mostTradedSymbol: ''
  };
  // Valeur retournée par l'API pour les joueurs actifs (source de vérité quand disponible)
  private apiActivePlayersCount = 0;

  // Modal joueurs actifs
  showActivePlayersModal = false;
  activePlayers: any[] = [];
  activePlayersLoading = false;

  // Auto-refresh
  autoRefresh = true;
  refreshInterval = 5000; // ms
  private subscriptions = new Subscription();

  constructor(
    private gameMasterService: GameMasterService,
    private orderBookService: OrderBookService,
    private marketStockService: MarketStockService,
    private toastService: ToastService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadSymbols();
    this.loadStats();
    this.loadAllOrders(); // Charger TOUS les ordres
    
    // Auto-refresh des carnets d'ordres
    if (this.autoRefresh) {
      const refreshSub = interval(this.refreshInterval).subscribe(() => {
        if (this.selectedSymbol) {
          this.loadOrderBook(this.selectedSymbol);
        }
        this.loadStats();
        this.loadAllOrders(); // Refresh tous les ordres
      });
      this.subscriptions.add(refreshSub);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadSymbols(): void {
    this.marketStockService.getAllStocks().subscribe({
      next: (stocks: Stock[]) => {
        this.symbols = stocks;
        if (stocks.length > 0 && !this.selectedSymbol) {
          // Chercher AAPL en premier, sinon prendre le premier
          const appleStock = stocks.find(s => s.symbol === 'AAPL');
          const defaultStock = appleStock || stocks[0];
          
          this.selectedSymbol = defaultStock.symbol;
          this.fictiveOrderForm.symbol = defaultStock.symbol;
          console.log(`🎯 Symbole par défaut sélectionné: ${defaultStock.symbol}`);
          this.loadOrderBook(this.selectedSymbol);
        }
      },
      error: (err: any) => {
        console.error('Erreur chargement symboles:', err);
        this.toastService.error('Erreur chargement des symboles');
      }
    });
  }

  loadOrderBook(symbol: string): void {
    console.log(`📖 Chargement de TOUS les ordres depuis BDD pour ${symbol}...`);
    
    // D'abord charger les stocks pour avoir la correspondance stockId → symbol
    this.http.get<any[]>(`${environment.apiUrl}/market/stocks`).pipe(
      switchMap((stocks: any[]) => {
        console.log(`📦 ${stocks.length} stocks chargés`);
        
        // Trouver le stock correspondant au symbole
        const targetStock = stocks.find(s => s.symbol === symbol);
        if (!targetStock) {
          console.error(`❌ Stock introuvable pour symbole: ${symbol}`);
          throw new Error(`Stock ${symbol} introuvable`);
        }
        
        console.log(`✅ Stock trouvé: ${targetStock.symbol}`);
        console.log(`🔍 Le backend ne renvoie pas d'ID - on va matcher par SYMBOL`);
        
        // Maintenant charger TOUS les ordres
        return this.http.get<any[]>(`${environment.apiUrl}/allorders`).pipe(
          map(allOrders => ({ allOrders, targetStock, symbol, stocks }))
        );
      })
    ).subscribe({
      next: ({ allOrders, targetStock, symbol, stocks }: any) => {
        console.log(`📜 Total ordres en BDD: ${allOrders.length}`);
        
        // DEBUG: Afficher les 10 premiers ordres
        console.log('🔍 10 premiers ordres:');
        allOrders.slice(0, 10).forEach((o: any, idx: any) => {
          console.log(`   ${idx+1}. ID ${o.id}: ${o.side} ${o.quantity} stockId=${o.stockId} symbol=${o.stock?.symbol} @ ${o.price} - Status: ${o.status}`);
        });
        
        // Filtrer par SYMBOL (car le backend ne renvoie pas d'ID dans /market/stocks)
        const symbolOrders = allOrders.filter((o: any) => {
          // Essayer d'abord stock.symbol, sinon symbol direct
          const orderSymbol = o.stock?.symbol || o.symbol;
          return orderSymbol === symbol;
        });
        
        console.log(`✅ ${symbolOrders.length} ordres trouvés pour ${symbol} (filtré par symbol)`);
        
        // Compter par status
        const byStatus: any = {};
        symbolOrders.forEach((o: any) => {
          byStatus[o.status] = (byStatus[o.status] || 0) + 1;
        });
        console.log(`   - Par status:`, byStatus);
        
        // Grouper par prix (TOUS les ordres, peu importe le status)
        const bidMap = new Map<string, number>();
        const askMap = new Map<string, number>();
        
        symbolOrders.forEach((order: any) => {
          let price = order.price?.toString();
          const qty = parseFloat(order.remainingQuantity || order.quantity || 0);
          
          // Ignorer les ordres sans quantité
          if (qty <= 0) return;
          
          // 🔧 Pour les ordres MARKET ou sans prix
          if (!price || price === 'null' || parseFloat(price) <= 0) {
            const stockLastPrice = order.stock?.lastPrice;
            if (stockLastPrice && stockLastPrice > 0) {
              price = stockLastPrice.toString();
            } else {
              // Utiliser un prix placeholder visible
              price = order.side === 'BUY' ? '999999' : '0.01';
            }
          }
          
          if (order.side === 'BUY') {
            bidMap.set(price, (bidMap.get(price) || 0) + qty);
          } else if (order.side === 'SELL') {
            askMap.set(price, (askMap.get(price) || 0) + qty);
          }
        });
        
        // Convertir en arrays
        const bids: OrderBookLevel[] = [];
        const asks: OrderBookLevel[] = [];
        
        bidMap.forEach((quantity, price) => bids.push({ price, quantity }));
        askMap.forEach((quantity, price) => asks.push({ price, quantity }));
        
        // Trier
        bids.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
        asks.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
        
        console.log(`   - ${bids.length} niveaux BID, ${asks.length} niveaux ASK`);
        
        const bestBid = bids.length > 0 ? parseFloat(bids[0].price) : 0;
        const bestAsk = asks.length > 0 ? parseFloat(asks[0].price) : 0;
        const spread = bestAsk && bestBid ? bestAsk - bestBid : 0;
        
        // Calculer le dernier prix
        const lastPrice = bestBid && bestAsk 
          ? (bestBid + bestAsk) / 2 
          : (symbolOrders.length > 0 && symbolOrders[0].price ? parseFloat(symbolOrders[0].price) : 0);

        const orderBookData: OrderBookData = {
          symbol,
          bids,
          asks,
          lastPrice,
          spread
        };

        this.orderBooks.set(symbol, orderBookData);
        this.currentOrderBook = orderBookData;
        
        // Mettre à jour le prix du marché actuel
        this.currentMarketPrice = lastPrice;
        
        // Recalculer les stats globales
        this.updateStats();
      },
      error: (err) => {
        console.error(`❌ Erreur chargement ordres:`, err);
        console.error(`   Status: ${err.status}`);
        console.error(`   Message: ${err.error?.message || err.message}`);
        this.toastService.error(`Erreur chargement des ordres: ${err.error?.message || err.statusText}`);
      }
    });
  }

  onSymbolChange(symbol: string): void {
    this.selectedSymbol = symbol;
    this.fictiveOrderForm.symbol = symbol;
    this.loadOrderBook(symbol);
  }



  onPriceTypeChange(): void {
    // Réinitialiser le prix limite ou suggérer le prix du marché
    if (this.fictiveOrderForm.priceType === 'LIMIT' && this.currentMarketPrice > 0) {
      this.fictiveOrderForm.limitPrice = this.currentMarketPrice;
    } else {
      this.fictiveOrderForm.limitPrice = 0;
    }
  }

  canInjectOrder(): boolean {
    // Vérifier que le symbole est sélectionné
    if (!this.fictiveOrderForm.symbol) return false;
    
    // Vérifier la quantité
    if (this.fictiveOrderForm.quantity <= 0) return false;
    
    // Si prix limite, vérifier qu'il est > 0
    if (this.fictiveOrderForm.priceType === 'LIMIT' && this.fictiveOrderForm.limitPrice <= 0) {
      return false;
    }
    
    return true;
  }

  loadStats(): void {
    this.gameMasterService.getMarketStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.apiActivePlayersCount = stats?.activePlayers || 0;

        // Harmoniser le nombre de joueurs actifs avec les ordres déjà chargés
        if (this.allOrders && this.allOrders.length > 0) {
          const localCount = this.computeActivePlayersCount(this.allOrders);
          this.stats.activePlayers = this.apiActivePlayersCount > 0
            ? Math.min(localCount, this.apiActivePlayersCount)
            : localCount;
        }
      },
      error: (err) => {
        console.error('Erreur chargement stats:', err);
      }
    });
  }

  /**
   * Calcule le nombre de joueurs uniques avec un playerId défini parmi les ordres actifs.
   */
  private computeActivePlayersCount(orders: Order[]): number {
    const activeOrders = orders.filter(o =>
      (o.status === 'PENDING' || o.status === 'PARTIALLY_FILLED') &&
      o.playerId !== null && o.playerId !== undefined && o.playerId > 0
    );
    return new Set(activeOrders.map(o => o.playerId)).size;
  }

  loadActivePlayers(): void {
    this.activePlayersLoading = true;
    this.gameMasterService.getActivePlayers().subscribe({
      next: (response) => {
        console.log('🔍 DEBUG - Response de getActivePlayers():', response);
        console.log('🔍 DEBUG - Type:', typeof response);
        console.log('🔍 DEBUG - Is Array?:', Array.isArray(response));
        console.log('🔍 DEBUG - Keys:', response ? Object.keys(response) : 'null');
        console.log('🔍 DEBUG - JSON:', JSON.stringify(response));
        
        // Essayer plusieurs formats possibles
        if (Array.isArray(response)) {
          this.activePlayers = response;
          console.log('✅ Format détecté: Array');
        } else if (response && typeof response === 'object') {
          // Chercher un tableau dans les propriétés de l'objet
          const keys = Object.keys(response);
          let found = false;
          
          for (const key of keys) {
            if (Array.isArray(response[key])) {
              console.log(`✅ Format détecté: Object.${key} = Array`);
              this.activePlayers = response[key];
              found = true;
              break;
            }
          }
          
          if (!found) {
            // Si aucun tableau trouvé et l'objet lui-même a des propriétés joueur
            if (response.id || response.name || response.username) {
              console.log('✅ Format détecté: Objet joueur unique');
              this.activePlayers = [response];
            } else {
              console.log('❌ Pas de joueur trouvé dans la réponse - FALLBACK MODE');
              // FALLBACK: Générer les joueurs actifs à partir des ordres
              this.generateActivePlayersFromOrders();
              found = true;
            }
          }
        } else {
          this.activePlayers = [];
          console.log('❌ Format inconnu - FALLBACK MODE');
          this.generateActivePlayersFromOrders();
        }
        
        this.activePlayersLoading = false;
        console.log(`✅ ${this.activePlayers.length} joueurs actifs chargés:`, this.activePlayers);
      },
      error: (err) => {
        console.error('❌ Erreur chargement joueurs actifs:', err);
        console.log('⚠️  Activation du FALLBACK MODE suite à erreur');
        this.generateActivePlayersFromOrders();
        this.activePlayersLoading = false;
      }
    });
  }

  /**
   * FALLBACK: Générer la liste des joueurs actifs à partir des ordres
   * Utilisé quand le backend ne retourne pas de joueurs
   */
  private generateActivePlayersFromOrders(): void {
    console.log('🔄 Génération des joueurs actifs à partir des ordres...');
    
    if (!this.allOrders || this.allOrders.length === 0) {
      console.warn('⚠️  Aucun ordre disponible pour générer la liste des joueurs');
      this.activePlayers = [];
      return;
    }

    // Filtrer les ordres actifs (PENDING ou PARTIALLY_FILLED)
    const activeOrders = this.allOrders.filter(o => 
      (o.status === 'PENDING' || o.status === 'PARTIALLY_FILLED') &&
      o.playerId !== null && o.playerId !== undefined && o.playerId > 0
    );

    // Créer une map unique de joueurs à partir des ordres (ignore les playerId nuls)
    const playerMap = new Map<number, any>();
    
    activeOrders.forEach(order => {
      if ((order.playerId !== null && order.playerId !== undefined && order.playerId > 0) && !playerMap.has(order.playerId)) {
        playerMap.set(order.playerId, {
          playerId: order.playerId,
          username: order.player?.username || `Player_${order.playerId}`,
          email: `player_${order.playerId}@trading.local`,
          role: 'PLAYER',
          lastLogin: order.createdAt,
          lastActivity: order.updatedAt || order.createdAt,
          isActive: true
        });
      }
    });

    // Convertir la map en array
    this.activePlayers = Array.from(playerMap.values());
    console.log(`✅ FALLBACK: ${this.activePlayers.length} joueurs actifs générés à partir des ordres`);
  }

  openActivePlayersModal(): void {
    this.showActivePlayersModal = true;
    this.loadActivePlayers();
  }

  closeActivePlayersModal(): void {
    this.showActivePlayersModal = false;
  }

  loadAllOrders(): void {
    this.allOrdersLoading = true;
    this.http.get<Order[]>(`${environment.apiUrl}/allorders`).subscribe({
      next: (orders) => {
        this.allOrders = orders;
        this.allOrdersLoading = false;
        console.log(`✅ ${orders.length} ordres chargés depuis la BDD`);
        // Calculer les statistiques après chargement des ordres
        this.updateStats();
      },
      error: (err) => {
        console.error('❌ Erreur chargement tous les ordres:', err);
        this.allOrdersLoading = false;
        this.toastService.error('Erreur chargement des ordres');
      }
    });
  }

  /**
   * 📊 Calcule les statistiques globales depuis allOrders
   */
  updateStats(): void {
    // Filtrer uniquement les ordres actifs (PENDING, PARTIALLY_FILLED)
    const activeOrders = this.allOrders.filter(o => 
      (o.status === 'PENDING' || o.status === 'PARTIALLY_FILLED') &&
      o.playerId !== null && o.playerId !== undefined && o.playerId > 0
    );

    // 1. Total ordres actifs
    this.stats.totalOrders = activeOrders.length;

    // 2. Volume total (somme des quantités restantes)
    this.stats.totalVolume = activeOrders.reduce((sum, o) => 
      sum + (o.remainingQuantity || o.quantity || 0), 0
    );

    // 3. Joueurs actifs (uniquement ceux avec un playerId défini)
    const localActivePlayers = this.computeActivePlayersCount(activeOrders);
    this.stats.activePlayers = this.apiActivePlayersCount > 0
      ? Math.min(localActivePlayers, this.apiActivePlayersCount)
      : localActivePlayers;

    // 4. Symbole le plus échangé (par volume)
    const volumeBySymbol = new Map<string, number>();
    activeOrders.forEach(o => {
      const symbol = o.stock?.symbol || 'N/A';
      const volume = volumeBySymbol.get(symbol) || 0;
      volumeBySymbol.set(symbol, volume + (o.remainingQuantity || o.quantity || 0));
    });

    let maxVolume = 0;
    let topSymbol = '-';
    volumeBySymbol.forEach((volume, symbol) => {
      if (volume > maxVolume) {
        maxVolume = volume;
        topSymbol = symbol;
      }
    });
    this.stats.mostTradedSymbol = topSymbol;

    console.log('📊 Stats mises à jour:', this.stats);
  }

  // Injection d'ordres fictifs
  injectFictiveOrder(): void {
    if (!this.fictiveOrderForm.symbol) {
      this.toastService.warning('Veuillez sélectionner un symbole');
      return;
    }

    // Validation du prix limite
    if (this.fictiveOrderForm.priceType === 'LIMIT') {
      if (!this.fictiveOrderForm.limitPrice || this.fictiveOrderForm.limitPrice <= 0) {
        this.toastService.error('Le prix limite doit être supérieur à 0');
        return;
      }
    }

    // Construire le payload - IMPORTANT: Ne pas envoyer limitPrice si MARKET
    const payload: any = {
      symbol: this.fictiveOrderForm.symbol,
      side: this.fictiveOrderForm.side,
      quantity: Math.abs(this.fictiveOrderForm.quantity),
      priceType: this.fictiveOrderForm.priceType,
      description: this.fictiveOrderForm.description
    };

    // Ajouter limitPrice UNIQUEMENT si type LIMIT
    if (this.fictiveOrderForm.priceType === 'LIMIT') {
      payload.limitPrice = this.fictiveOrderForm.limitPrice;
    }

    console.log('📤 Envoi ordre fictif:', payload);
    console.log('   - Symbole sélectionné:', this.selectedSymbol);
    console.log('   - Symbole du form:', this.fictiveOrderForm.symbol);

    this.gameMasterService.injectFictiveOrder(payload).subscribe({
      next: (response) => {
        console.log('✅ Réponse backend:', response);
        this.toastService.success(
          `✅ Ordre fictif créé: ${payload.side} ${payload.quantity} ${payload.symbol}`,
          5000
        );
        
        // Attendre 1 seconde pour laisser le backend terminer le matching
        console.log('⏳ Attente 1s avant rechargement du carnet...');
        setTimeout(() => {
          this.loadOrderBook(this.selectedSymbol);
          this.loadStats();
        }, 1000);
      },
      error: (err) => {
        console.error('❌ Erreur injection ordre fictif:', err);
        const errorMsg = err.error?.message || err.message || 'Erreur inconnue';
        this.toastService.error(`Erreur: ${errorMsg}`);
      }
    });
  }

  // Déclencher un événement de marché
  // SUPPRIMÉ - Déplacé vers price-history.component.ts

  // Annuler tous les ordres d'un symbole
  cancelAllOrders(symbol: string): void {
    if (!confirm(`Annuler TOUS les ordres pour ${symbol} ?`)) {
      return;
    }

    this.gameMasterService.cancelAllOrders(symbol).subscribe({
      next: () => {
        this.toastService.success(`Tous les ordres ${symbol} annulés`);
        this.loadOrderBook(symbol);
      },
      error: (err) => {
        console.error('Erreur annulation ordres:', err);
        this.toastService.error('Erreur lors de l\'annulation');
      }
    });
  }

  // Helpers
  parseFloat = parseFloat; // Expose parseFloat to template

  getTotalBidVolume(): number {
    return this.currentOrderBook?.bids.reduce((sum, b) => sum + b.quantity, 0) || 0;
  }

  getTotalAskVolume(): number {
    return this.currentOrderBook?.asks.reduce((sum, a) => sum + a.quantity, 0) || 0;
  }

  getBidAskRatio(): number {
    const bidVol = this.getTotalBidVolume();
    const askVol = this.getTotalAskVolume();
    return askVol > 0 ? bidVol / askVol : 0;
  }

  getMarketSentiment(): string {
    const ratio = this.getBidAskRatio();
    if (ratio > 1.2) return '📈 Haussier';
    if (ratio < 0.8) return '📉 Baissier';
    return '➡️ Neutre';
  }

  formatNumber(num: number): string {
    return num.toLocaleString('fr-FR');
  }

  formatPrice(price: string | number): string {
    return typeof price === 'string' 
      ? parseFloat(price).toFixed(2) 
      : price.toFixed(2);
  }

  formatLastActivity(lastActivity: string): string {
    if (!lastActivity) return 'Inconnue';
    
    const now = new Date();
    const activityDate = new Date(lastActivity);
    const diffMs = now.getTime() - activityDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'À l\'instant';
    if (diffMins === 1) return 'Il y a 1 minute';
    if (diffMins < 60) return `Il y a ${diffMins} minutes`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours === 1) return 'Il y a 1 heure';
    if (diffHours < 24) return `Il y a ${diffHours} heures`;
    
    return activityDate.toLocaleDateString('fr-FR');
  }
}
