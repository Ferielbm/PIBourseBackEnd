import {
  Component,
  OnInit,
  OnDestroy,
  ElementRef,
  ViewChild,
  Renderer2
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl
} from '@angular/forms';
import { FormsModule } from '@angular/forms';

import { Date2023Pipe } from '../pipes/date-2023.pipe';
import { PriceAlert } from '../../../models/Order/market-alert.models';
import { MarketAlertService } from '../../../services/Order/market-alert.service';

// même Stock que dans le module Market
import { Stock } from '../../market/models/stock.models';
// utiliser le service central du module Market (retours + fallback)
import { MarketStockService } from '../../market/services/market-stock.service';
import { AuthService } from '../../../services/auth.service';

type AlertStatusFilter = 'ALL' | 'ACTIVE' | 'PAUSED' | 'CANCELLED';

@Component({
  selector: 'app-alert-market',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, Date2023Pipe],
  templateUrl: './alert-market.component.html',
  styleUrls: ['./alert-market.component.css']
})
export class AlertMarketComponent implements OnInit, OnDestroy {
  playerId = 0;

  alerts: PriceAlert[] = [];

  // symboles depuis la BDD
  symbols: Stock[] = [];

  loading = false;
  loadingSymbols = false;
  submitting = false;

  error = '';
  successMessage = '';

  searchTerm = '';
  statusFilter: AlertStatusFilter = 'ALL';

  form = this.fb.group(
    {
      symbol: ['', Validators.required],
      minPrice: [null],
      maxPrice: [null]
    },
    { validators: priceRangeValidator }
  );

  // état du dropdown de symboles
  symbolSearch = '';
  openSymbols = false;
  @ViewChild('symbolWrapper') symbolWrapper!: ElementRef;
  private docClickUnlisten?: () => void;

  constructor(
    private fb: FormBuilder,
    private alertService: MarketAlertService,
    private marketStockService: MarketStockService,
    private renderer: Renderer2,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.playerId = this.authService.getCurrentPlayerId() || 0;
    this.loadSymbols();
    this.loadAlerts();

    // fermeture du dropdown quand on clique à l'extérieur
    this.docClickUnlisten = this.renderer.listen(
      'document',
      'click',
      (event: Event) => {
        try {
          const target = event.target as Node;
          if (
            this.openSymbols &&
            this.symbolWrapper &&
            !this.symbolWrapper.nativeElement.contains(target)
          ) {
            this.openSymbols = false;
          }
        } catch {
          // ignore
        }
      }
    );
  }

  ngOnDestroy(): void {
    if (this.docClickUnlisten) {
      this.docClickUnlisten();
    }
  }

  // --- CHARGER LES SYMBOLES POUR LA LISTE DÉROULANTE ---
  loadSymbols(): void {
    this.loadingSymbols = true;

    this.marketStockService.getAllStocks().subscribe({
      next: (stocks: Stock[]) => {
        console.log('Symboles chargés (alert-market):', stocks);
        this.symbols = stocks;
        this.loadingSymbols = false;
      },
      error: (err: any) => {
        console.error('Erreur chargement symboles:', err);
        this.loadingSymbols = false;
      }
    });
  }

  // --- SEARCH pour le dropdown ---
  onSymbolSearchChange(term: string): void {
    this.symbolSearch = term;
    this.openSymbols = true;
  }

  get filteredSymbols(): Stock[] {
    const term = (this.symbolSearch || '').trim().toLowerCase();
    if (!term) {
      return this.symbols.slice(0, 200);
    }
    return this.symbols.filter((s) => {
      const label = (s.symbol + ' ' + (s.name || '')).toLowerCase();
      return label.includes(term);
    });
  }

  selectSymbol(s: Stock): void {
    this.form.get('symbol')?.setValue(s.symbol);
    this.symbolSearch = s.symbol + (s.name ? ' - ' + s.name : '');
    this.openSymbols = false;
  }

  // --- CHARGER LES ALERTES ---
  loadAlerts(): void {
    this.loading = true;
    this.error = '';

    this.alertService.getAlerts(this.playerId).subscribe({
      next: (data) => {
        this.alerts = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement alertes:', err);
        this.loading = false;
        this.error =
          err.error?.message || 'Erreur lors du chargement des alertes.';
      }
    });
  }

  // --- LISTE FILTRÉE ---
  get filteredAlerts(): PriceAlert[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.alerts.filter((a) => {
      const status = String(a.status || 'ACTIVE');
      const symbol = (a.symbol || '').toLowerCase();

      if (this.statusFilter !== 'ALL' && status !== this.statusFilter) {
        return false;
      }
      if (term && !symbol.includes(term)) {
        return false;
      }
      return true;
    });
  }

  // --- CRÉER UNE ALERTE ---
  createAlert(): void {
    this.error = '';
    this.successMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.value;

    const alertData: PriceAlert = {
      symbol: (value.symbol || '').toUpperCase().trim(),
      minPrice: value.minPrice ?? null,
      maxPrice: value.maxPrice ?? null
    };

    this.submitting = true;

    this.alertService.createAlert(this.playerId, alertData).subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = '✅ Alerte créée avec succès !';
        this.form.reset();
        this.form.markAsPristine();
        this.form.markAsUntouched();
        this.symbolSearch = '';
        this.loadAlerts();
      },
      error: (err) => {
        console.error('Erreur création alerte:', err);
        this.submitting = false;
        this.error =
          err.error?.message || 'Erreur lors de la création de l’alerte.';
      }
    });
  }

  // --- SUPPRIMER ---
  deleteAlert(alertId: number | undefined): void {
    if (!alertId) return;
    if (!window.confirm('Supprimer cette alerte ?')) return;

    this.alertService.deleteAlert(this.playerId, alertId).subscribe({
      next: () => this.loadAlerts(),
      error: (err) => {
        console.error('Erreur suppression alerte:', err);
        window.alert('Erreur lors de la suppression.');
      }
    });
  }

  // --- CHANGER STATUT ---
  changeStatus(
    a: PriceAlert,
    status: 'ACTIVE' | 'PAUSED' | 'CANCELLED'
  ): void {
    if (!a.id) return;

    this.alertService.updateStatus(this.playerId, a.id, status).subscribe({
      next: () => this.loadAlerts(),
      error: (err) => {
        console.error('Erreur changement statut:', err);
        window.alert('Erreur lors du changement de statut.');
      }
    });
  }

  get formErrors() {
    return this.form.errors;
  }

  hasControlError(controlName: string): boolean {
    const c = this.form.get(controlName);
    return !!(c && c.touched && c.invalid);
  }
}

// --- VALIDATEUR GLOBAL POUR min/max ---
export function priceRangeValidator(group: AbstractControl) {
  const min = group.get('minPrice')?.value;
  const max = group.get('maxPrice')?.value;

  if ((min == null || min === '') && (max == null || max === '')) {
    return { priceRequired: true };
  }

  if (
    min != null &&
    min !== '' &&
    max != null &&
    max !== '' &&
    Number(min) > Number(max)
  ) {
    return { invalidRange: true };
  }

  return null;
}
