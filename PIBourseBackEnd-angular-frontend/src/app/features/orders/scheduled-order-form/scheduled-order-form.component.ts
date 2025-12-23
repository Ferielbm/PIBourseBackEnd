import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ScheduledOrderRequest, ScheduledOrderService } from '../../../services/Order/scheduled-order.service';
import { ScheduledOrdersStateService } from '../../../services/Order/scheduled-orders-state.service';
import { Stock } from '../../market/models/stock.models';
import { MarketStockService } from '../../market/services/market-stock.service';

@Component({
  selector: 'app-scheduled-order-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './scheduled-order-form.component.html',
  styleUrls: ['./scheduled-order-form.component.css']
})
export class ScheduledOrderFormComponent implements OnInit {
  @Input() playerId: number = 0;

  form: FormGroup;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  sides = ['BUY', 'SELL'];
  symbols: Stock[] = [];
  loadingSymbols = false;

  constructor(
    private fb: FormBuilder,
    private scheduledOrderService: ScheduledOrderService,
    private stateService: ScheduledOrdersStateService,
    private marketStockService: MarketStockService
  ) {
    this.form = this.fb.group({
      symbol: ['', Validators.required],
      side: ['BUY', Validators.required],
      quantity: [null, [Validators.required, Validators.min(0.000001)]],
      minPrice: [null],
      maxPrice: [null],
      notifyOnly: [false], // ✅ Exécution automatique par défaut
      notifyWhenApproachMin: [false],
      approachThresholdPct: [0.05],
      approachCooldownMinutes: [60]
    });
  }

  ngOnInit(): void {
    this.loadSymbols();
  }
loadSymbols(): void {
  this.loadingSymbols = true;
  this.marketStockService.getAllStocks().subscribe({
    next: (stocks: Stock[]) => {
      console.log('Symboles chargés (scheduled-order):', stocks);
      this.symbols = stocks;
      this.loadingSymbols = false;
    },
    error: (err: any) => {
      console.error('Erreur chargement symboles (scheduled-order):', err);
      this.loadingSymbols = false;
    }
  });}
  

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.value;

    const payload: ScheduledOrderRequest = {
      symbol: raw.symbol,
      side: raw.side,
      quantity: raw.quantity,
      minPrice: raw.minPrice,
      maxPrice: raw.maxPrice,
      notifyOnly: raw.notifyOnly,
      notifyWhenApproachMin: raw.notifyWhenApproachMin,
      approachThresholdPct: raw.approachThresholdPct,
      approachCooldownMinutes: raw.approachCooldownMinutes
    };

    this.isSubmitting = true;

    console.log('Creation pour playerId:', this.playerId);
    
    this.scheduledOrderService.createScheduledOrder(this.playerId, payload).subscribe({
      next: (response) => {
        console.log('Reponse du serveur:', response);
        this.isSubmitting = false;
        this.successMessage = 'Ordre planifie cree avec succes !';
        
        this.stateService.notifyOrderCreated();
        
        this.scheduledOrderService.getScheduledOrders(this.playerId).subscribe({
          next: (orders) => {
            console.log('Ordres recuperes:', orders);
          },
          error: (err) => {
            console.error('Erreur lors de la recuperation des ordres:', err);
          }
        });
        
        this.form.reset({
          side: 'BUY',
          notifyOnly: false, // ✅ Exécution automatique par défaut
          notifyWhenApproachMin: false,
          approachThresholdPct: 0.05,
          approachCooldownMinutes: 60
        });
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Erreur creation:', err);
        this.errorMessage = err?.error?.message || 'Erreur lors de la creation de l\'ordre planifie.';
      }
    });
  }

  showApproachFields(): boolean {
    return this.form.get('notifyWhenApproachMin')?.value === true;
  }
}
