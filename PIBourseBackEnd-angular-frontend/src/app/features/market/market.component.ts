import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from '../../services/Order/order.service';
import { PlaceOrderRequest } from '../../models/Order/order.models';
import { MarketStockService } from './services/market-stock.service';
import { Stock } from './models/stock.models';
import { AuthService } from '../../services/auth.service';
import { OrderBookDisplayComponent } from '../../components/Market/order-book-display/order-book-display.component';
import { CandlestickChartComponent } from '../../components/Market/candlestick-chart/candlestick-chart.component';
import { WalletSummaryComponent } from '../../components/wallet-summary/wallet-summary.component';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, OrderBookDisplayComponent, CandlestickChartComponent, WalletSummaryComponent],
  templateUrl: './market.component.html',
  styleUrls: ['./market.component.css']
})
export class MarketComponent implements OnInit {
  playerId = 0;
  form: FormGroup;
  symbols: Stock[] = [];
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  currentPrice = 0;
  // polling map pour surveiller le statut des ordres créés
  private orderPollingMap: Map<number, number> = new Map();
  
  constructor(
    private fb: FormBuilder,
    private orderService: OrderService,
    private marketStockService: MarketStockService,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.form = this.fb.group({
      symbol: ['AAPL', Validators.required],
      quantity: [10, [Validators.required, Validators.min(0.000001)]],
      side: ['BUY', Validators.required],
      price: [0, [Validators.required, Validators.min(0.000001)]]
    });
  }

  ngOnInit(): void {
    this.playerId = this.authService.getCurrentPlayerId() || 0;
    this.loadSymbols();
    
    // Check for symbol query parameter and apply it
    this.route.queryParamMap.subscribe(params => {
      const symbolFromUrl = params.get('symbol');
      if (symbolFromUrl) {
        console.log('📌 Symbole reçu depuis URL:', symbolFromUrl);
        this.form.patchValue({ symbol: symbolFromUrl });
        this.loadMarketPrice(symbolFromUrl);
      }
    });
    
    // Charger le prix quand le symbole change
    this.form.get('symbol')?.valueChanges.subscribe((symbol) => {
      console.log('🔁 symbol valueChanges:', symbol);
      this.loadMarketPrice(symbol);
    });
  }

  ngOnDestroy(): void {
    // clear tous les timers de polling
    this.orderPollingMap.forEach((timerId) => clearInterval(timerId));
    this.orderPollingMap.clear();
  }

  loadMarketPrice(symbol: string): void {
    if (!symbol) return;
    
    const stock = this.symbols.find(s => s.symbol === symbol);
    if (stock) {
      this.currentPrice = stock.lastPrice || 0;
      console.log(`Prix du marche pour ${symbol}: ${this.currentPrice}`);
      // Pré-remplir le prix limite avec le prix du marché (modifiable ensuite par le joueur)
      this.form.get('price')?.setValue(this.currentPrice, { emitEvent: false });
    }
  }

  loadSymbols(): void {
    this.marketStockService.getAllStocks().subscribe({
      next: (stocks: Stock[]) => {
        console.log('Symboles charges:', stocks);
        this.symbols = stocks;
        // Charger le prix dès que les symboles sont disponibles
        this.loadMarketPrice(this.form.get('symbol')?.value);
      },
      error: (err: any) => {
        console.error('Erreur chargement symboles:', err);
      }
    });
  }



  placeOrder(side: 'BUY' | 'SELL'): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.value;

    const payload: PlaceOrderRequest = {
      symbol: raw.symbol,
      side: side,
      type: 'LIMIT',
      tif: 'GTC',
      quantity: raw.quantity,
      price: raw.price
    };

    console.log('📤 Envoi de l\'ordre avec payload:', payload);
    this.isSubmitting = true;

    this.orderService.placeOrder(this.playerId, payload).subscribe({
      next: (response: any) => {
        console.log('✅ Ordre cree (reponse backend):', response);
        this.isSubmitting = false;

        // Si le backend ne retourne pas explicitement le statut, on affiche PENDING côté client
        const returnedStatus = response?.status || 'PENDING';

        const orderType = side === 'BUY' ? 'achat' : 'vente';
        this.successMessage = `Ordre ${orderType} créé avec succès (${returnedStatus})`;
        this.toastService.success(`✅ Ordre ${orderType} de ${payload.quantity} ${payload.symbol} créé !`);

        // réinitialiser le form
        this.form.reset({
          symbol: raw.symbol,
          quantity: 10,
          side: 'BUY',
          price: this.currentPrice
        });

        // Si on a un id, démarrer le polling du statut (pour détecter l'exécution automatique côté serveur)
        const orderId = response?.id;
        if (orderId && returnedStatus === 'PENDING') {
          this.startPollingOrderStatus(orderId);
        }
      },
      error: (err: any) => {
        this.isSubmitting = false;
        console.error('\u274c Erreur creation ordre:', err);
        
        const status = err?.status || '??';
        const errorData = err?.error || {};
        let userMessage = '';

        // Détecter les erreurs de solde insuffisant
        if (status === 400 && errorData.error && errorData.error.includes('Solde insuffisant')) {
          const match = errorData.error.match(/disponible=([\d.]+)/);
          const available = match ? parseFloat(match[1]) : 0;
          const required = this.getTotalAmount();
          
          userMessage = `❌ Solde insuffisant !\n\nDisponible: ${available.toFixed(2)}€\nRequis: ${required.toFixed(2)}€\nManquant: ${(required - available).toFixed(2)}€`;
          this.errorMessage = `Solde insuffisant. Vous avez ${available.toFixed(2)}€ mais ${required.toFixed(2)}€ sont nécessaires.`;
          this.toastService.error(userMessage);
        } else {
          // Autres erreurs
          const bodyMsg = errorData.message || errorData.error || err?.message || 'Erreur inconnue';
          userMessage = `Erreur ${status}: ${bodyMsg}`;
          this.errorMessage = userMessage;
          this.toastService.error(`❌ ${userMessage}`);
        }
      }
    });
  }

  /**
   * Démarre un polling périodique pour vérifier le statut d'un ordre jusqu'à ce
   * que le backend le marque comme FILLED / PARTIALLY_FILLED / CANCELLED.
   */
  private startPollingOrderStatus(orderId: number): void {
    if (this.orderPollingMap.has(orderId)) return;

    const intervalMs = 2000; // toutes les 2s
    const timer = window.setInterval(() => {
      this.orderService.getOrderById(this.playerId, orderId).subscribe({
        next: (order) => {
          if (!order) return;
          const s = order.status;
          console.log(`Polling order ${orderId} status:`, s);

          if (s && (s === 'FILLED' || s === 'PARTIALLY_FILLED' || s === 'CANCELLED' || s === 'CANCELED' || s === 'REJECTED')) {
            // stop polling
            const t = this.orderPollingMap.get(orderId);
            if (t) clearInterval(t);
            this.orderPollingMap.delete(orderId);

            // notifier l'utilisateur
            this.successMessage = `Ordre ${orderId} mis à jour: ${s}`;
          }
        },
        error: (err) => {
          console.error('Erreur polling statut ordre:', err);
        }
      });
    }, intervalMs);

    this.orderPollingMap.set(orderId, timer);
  }

  getTotalAmount(): number {
    const quantity = this.form.get('quantity')?.value || 0;
    const price = this.form.get('price')?.value || 0;
    return quantity * price;
  }

  getChartStartDate(): string {
    // 30 jours en arrière
    const date = new Date();
    date.setDate(date.getDate() - 30);
    return date.toISOString().slice(0, 19);
  }

  getChartEndDate(): string {
    // Aujourd'hui
    return new Date().toISOString().slice(0, 19);
  }
}