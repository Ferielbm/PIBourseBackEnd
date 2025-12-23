import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, interval, takeUntil } from 'rxjs';
import { Date2023Pipe } from '../pipes/date-2023.pipe';
import { ScheduledOrder } from '../../../models/Order/scheduled-order.models';
import { ScheduledOrderService } from '../../../services/Order/scheduled-order.service';
import { ScheduledOrderRefreshService } from '../../../services/Order/scheduled-order-refresh.service';
import { AuthService } from '../../../services/auth.service';


type SideFilter = 'ALL' | 'BUY' | 'SELL';
type StatusFilter = 'ALL' | 'PENDING' | 'TRIGGERED' | 'EXECUTED' | 'REFUSED' | 'CANCELLED' | 'FAILED';

@Component({
  selector: 'app-scheduled-orders-list',
  standalone: true,
  imports: [CommonModule, FormsModule, Date2023Pipe],
  templateUrl: './scheduled-orders-list.component.html',
  styleUrls: ['./scheduled-orders-list.component.css']
})
export class ScheduledOrdersListComponent implements OnInit, OnDestroy {
  @Input() playerId: number = 0;

  scheduledOrders: ScheduledOrder[] = [];
  isLoading = false;
  errorMessage = '';

  // 🔎 recherche + filtres
  searchTerm = '';
  sideFilter: SideFilter = 'ALL';
  statusFilter: StatusFilter = 'ALL';
  
  private destroy$ = new Subject<void>();

  constructor(
    private scheduledOrderService: ScheduledOrderService,
    private refreshService: ScheduledOrderRefreshService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Récupérer le playerId du joueur connecté
    if (!this.playerId) {
      this.playerId = this.authService.getCurrentPlayerId() || 0;
    }
    this.loadScheduledOrders();
    
    // Rafraîchir tous les 5 secondes
    interval(5000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadScheduledOrders());
    
    // Écouter les demandes de rafraîchissement du service
    this.refreshService.refresh$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        console.log('🔄 Rafraîchissement demandé par le service');
        this.loadScheduledOrders();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadScheduledOrders(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.scheduledOrderService.getScheduledOrders(this.playerId).subscribe({
      next: (orders) => {
        console.log('Ordres charges:', orders);
        // Trier du plus récent au plus ancien
        this.scheduledOrders = orders.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement ordres:', err);
        this.errorMessage = err?.error?.message || 'Erreur lors du chargement des ordres planifies.';
        this.isLoading = false;
      }
    });
  }

  // 🔹 ordres filtrés
  get filteredOrders(): ScheduledOrder[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.scheduledOrders.filter(order => {
      const side = String(order.side || '');
      const status = String(order.status || '');
      const symbol = (order.desiredSymbol || '').toLowerCase();

      if (this.sideFilter !== 'ALL' && side !== this.sideFilter) return false;
      if (this.statusFilter !== 'ALL' && status !== this.statusFilter) return false;
      if (term && !symbol.includes(term)) return false;

      return true;
    });
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PENDING': 'text-blue-600 bg-blue-50',
      'TRIGGERED': 'text-green-600 bg-green-50',
      'EXECUTED': 'text-gray-700 bg-gray-100',
      'CANCELLED': 'text-gray-600 bg-gray-50',
      'FAILED': 'text-red-600 bg-red-50'
    };
    return colors[status] || 'text-gray-600';
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'TRIGGERED': return 'Déclenché';
      case 'EXECUTED': return 'Exécuté';
      case 'REFUSED': return 'Refusé';
      case 'CANCELLED':
      case 'CANCELED': return 'Annulé';
      case 'FAILED': return 'Échoué';
      default: return status;
    }
  }

  getSideLabel(side: string): string {
    return side === 'BUY' ? 'Achat' : 'Vente';
  }

  getSideBadgeClass(side: string): string {
    return side === 'BUY'
      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300'
      : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300';
  }

  getStatusBadgeClass(status: string): string {
    const st = String(status);
    switch (st) {
      case 'PENDING':
        return 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300';
      case 'TRIGGERED':
        return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300';
      case 'EXECUTED':
        return 'bg-gray-200 text-gray-700 dark:bg-gray-700 dark:text-gray-200';
      case 'REFUSED':
        return 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-300';
      case 'CANCELLED':
      case 'CANCELED':
        return 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300';
      case 'FAILED':
        return 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300';
      default:
        return 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300';
    }
  }

  reactivateOrder(orderId: number): void {
    if (confirm('Réactiver cet ordre pour recevoir un nouveau ticket de décision ?')) {
      this.scheduledOrderService.reactivateScheduledOrder(this.playerId, orderId).subscribe({
        next: () => {
          console.log('🔄 Ordre réactivé:', orderId);
          this.loadScheduledOrders();
        },
        error: (err) => {
          console.error('❌ Erreur réactivation ordre:', err);
          alert('Erreur lors de la réactivation de l\'ordre');
        }
      });
    }
  }

  deleteOrder(orderId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet ordre ?')) {
      this.scheduledOrderService.deleteScheduledOrder(this.playerId, orderId).subscribe({
        next: () => {
          console.log('✅ Ordre supprime:', orderId);
          this.loadScheduledOrders();
        },
        error: (err) => {
          console.error('❌ Erreur suppression ordre:', err);
          alert('Erreur lors de la suppression de l\'ordre');
        }
      });
    }
  }

  refresh(): void {
    this.loadScheduledOrders();
  }
}
