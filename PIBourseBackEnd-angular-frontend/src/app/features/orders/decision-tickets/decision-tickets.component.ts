import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DecisionTicketService, DecisionTicket } from '../../../services/Order/decision-ticket.service';
import { OrderExecutionNotificationService } from '../../../services/order-execution-notification.service';
import { ToastService } from '../../../services/toast.service';
import { ScheduledOrderRefreshService } from '../../../services/Order/scheduled-order-refresh.service';
import { AuthService } from '../../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-decision-tickets',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="decision-tickets-container">
      <h2>🎫 Décisions en attente</h2>

      <div *ngIf="tickets.length === 0" class="empty-state">
        <p>Aucune décision en attente</p>
      </div>

      <div *ngFor="let ticket of tickets" class="ticket-card" [class.approaching]="ticket.reason === 'APPROACHING_MIN'">
        <div class="ticket-header">
          <div class="ticket-info">
            <h3>{{ ticket.symbol }} {{ ticket.side }}</h3>
            <span class="reason-badge" [class.in-range]="ticket.reason === 'IN_RANGE'">
              {{ ticket.reason === 'IN_RANGE' ? '🎯 Prix atteint' : '⚠️ Approche' }}
            </span>
          </div>
          <div class="ticket-price">
            <span class="price-label">Prix trouvé</span>
            <span class="price-value">{{ ticket.foundPrice | number:'1.2-2' }}€</span>
          </div>
        </div>

        <div class="ticket-details">
          <div class="detail-item">
            <span class="label">Quantité suggérée:</span>
            <input 
              type="number" 
              [(ngModel)]="ticketQuantities[ticket.id]" 
              [placeholder]="ticket.suggestedQuantity.toString()"
              min="1"
              class="quantity-input"
            />
          </div>
        </div>

        <div class="ticket-actions">
          <button 
            class="btn-reject" 
            (click)="rejectTicket(ticket)"
            [disabled]="processingTickets[ticket.id]"
          >
            ❌ Refuser
          </button>
          <button 
            class="btn-accept" 
            (click)="acceptTicket(ticket)"
            [disabled]="processingTickets[ticket.id]"
          >
            ✅ Accepter et acheter
          </button>
        </div>

        <div *ngIf="processingTickets[ticket.id]" class="processing-overlay">
          Traitement en cours...
        </div>
      </div>
    </div>
  `,
  styles: [`
    .decision-tickets-container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    h2 {
      margin-bottom: 24px;
      color: #1f2937;
      font-size: 24px;
      font-weight: 600;
    }

    :host-context(.dark) h2 {
      color: #f9fafb;
    }

    .empty-state {
      text-align: center;
      padding: 60px 20px;
      color: #6b7280;
      font-size: 16px;
    }

    :host-context(.dark) .empty-state {
      color: #9ca3af;
    }

    .ticket-card {
      background: white;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 16px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      position: relative;
      transition: all 0.3s ease;
    }

    :host-context(.dark) .ticket-card {
      background: #1f2937;
      border-color: #374151;
      box-shadow: 0 1px 3px rgba(0,0,0,0.3);
    }

    .ticket-card:hover {
      border-color: #3b82f6;
      box-shadow: 0 4px 6px rgba(59, 130, 246, 0.1);
    }

    :host-context(.dark) .ticket-card:hover {
      border-color: #60a5fa;
      box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2);
    }

    .ticket-card.approaching {
      border-color: #f59e0b;
      background: #fffbeb;
    }

    :host-context(.dark) .ticket-card.approaching {
      border-color: #f59e0b;
      background: #422006;
    }

    .ticket-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
    }

    .ticket-info h3 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
    }

    :host-context(.dark) .ticket-info h3 {
      color: #f9fafb;
    }

    .reason-badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 500;
      background: #fef3c7;
      color: #92400e;
    }

    :host-context(.dark) .reason-badge {
      background: #78350f;
      color: #fef3c7;
    }

    .reason-badge.in-range {
      background: #dbeafe;
      color: #1e40af;
    }

    :host-context(.dark) .reason-badge.in-range {
      background: #1e3a8a;
      color: #dbeafe;
    }

    .ticket-price {
      text-align: right;
    }

    .price-label {
      display: block;
      font-size: 12px;
      color: #6b7280;
      margin-bottom: 4px;
    }

    :host-context(.dark) .price-label {
      color: #9ca3af;
    }

    .price-value {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: #059669;
    }

    :host-context(.dark) .price-value {
      color: #34d399;
    }

    .ticket-details {
      margin-bottom: 16px;
    }

    .detail-item {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
    }

    .detail-item .label {
      font-size: 14px;
      color: #6b7280;
      min-width: 150px;
    }

    :host-context(.dark) .detail-item .label {
      color: #9ca3af;
    }

    .quantity-input {
      padding: 8px 12px;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-size: 16px;
      width: 120px;
      transition: border-color 0.2s;
      background: white;
      color: #1f2937;
    }

    :host-context(.dark) .quantity-input {
      background: #111827;
      border-color: #4b5563;
      color: #f9fafb;
    }

    .quantity-input:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    :host-context(.dark) .quantity-input:focus {
      border-color: #60a5fa;
      box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
    }

    .ticket-actions {
      display: flex;
      gap: 12px;
      margin-top: 16px;
    }

    .ticket-actions button {
      flex: 1;
      padding: 12px 20px;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-reject {
      background: #fee2e2;
      color: #991b1b;
    }

    :host-context(.dark) .btn-reject {
      background: #7f1d1d;
      color: #fecaca;
    }

    .btn-reject:hover:not(:disabled) {
      background: #fecaca;
      transform: translateY(-1px);
      box-shadow: 0 4px 6px rgba(220, 38, 38, 0.2);
    }

    :host-context(.dark) .btn-reject:hover:not(:disabled) {
      background: #991b1b;
      box-shadow: 0 4px 6px rgba(127, 29, 29, 0.4);
    }

    .btn-accept {
      background: #10b981;
      color: white;
    }

    :host-context(.dark) .btn-accept {
      background: #059669;
      color: #d1fae5;
    }

    .btn-accept:hover:not(:disabled) {
      background: #059669;
      transform: translateY(-1px);
      box-shadow: 0 4px 6px rgba(16, 185, 129, 0.3);
    }

    :host-context(.dark) .btn-accept:hover:not(:disabled) {
      background: #047857;
      box-shadow: 0 4px 6px rgba(5, 150, 105, 0.4);
    }

    .ticket-actions button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .processing-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      font-weight: 600;
      color: #3b82f6;
    }

    :host-context(.dark) .processing-overlay {
      background: rgba(31, 41, 55, 0.95);
      color: #60a5fa;
    }

    @media (max-width: 640px) {
      .ticket-header {
        flex-direction: column;
        gap: 12px;
      }

      .ticket-price {
        text-align: left;
      }

      .detail-item {
        flex-direction: column;
        align-items: flex-start;
      }

      .quantity-input {
        width: 100%;
      }
    }
  `]
})
export class DecisionTicketsComponent implements OnInit, OnDestroy {
  tickets: DecisionTicket[] = [];
  ticketQuantities: { [ticketId: number]: number } = {};
  processingTickets: { [ticketId: number]: boolean } = {};
  private subscriptions = new Subscription();
  private playerId = 0; // Sera initialisé depuis AuthService

  constructor(
    private decisionTicketService: DecisionTicketService,
    private notificationService: OrderExecutionNotificationService,
    private toastService: ToastService,
    private scheduledOrderRefreshService: ScheduledOrderRefreshService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.playerId = this.authService.getCurrentPlayerId() || 0;
    this.loadTickets();
    
    // S'abonner aux nouveaux tickets via WebSocket
    const ticketSub = this.notificationService.decisionTicket$.subscribe(notification => {
      console.log('🎫 Nouveau ticket reçu:', notification);
      this.loadTickets(); // Recharger la liste
    });
    this.subscriptions.add(ticketSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadTickets(): void {
    this.decisionTicketService.getPendingTickets(this.playerId).subscribe({
      next: (tickets: DecisionTicket[]) => {
        this.tickets = tickets;
        console.log(`✅ ${tickets.length} ticket(s) chargé(s)`);
      },
      error: (err) => {
        console.error('❌ Erreur chargement tickets:', err);
        this.toastService.error('Erreur lors du chargement des tickets');
      }
    });
  }

  acceptTicket(ticket: DecisionTicket): void {
    this.processingTickets[ticket.id] = true;
    const quantity = this.ticketQuantities[ticket.id] || ticket.suggestedQuantity;

    this.decisionTicketService.acceptTicket(this.playerId, ticket.id, quantity).subscribe({
      next: () => {
        this.toastService.success(`✅ Ordre ${ticket.symbol} ${ticket.side} passé avec succès`);
        this.loadTickets(); // Recharger pour retirer le ticket
        this.scheduledOrderRefreshService.triggerRefresh(); // Rafraîchir la liste des ordres planifiés
        delete this.processingTickets[ticket.id];
      },
      error: (err: any) => {
        console.error('❌ Erreur acceptation:', err);
        this.toastService.error('Erreur lors de l\'acceptation du ticket');
        delete this.processingTickets[ticket.id];
      }
    });
  }

  rejectTicket(ticket: DecisionTicket): void {
    this.processingTickets[ticket.id] = true;

    this.decisionTicketService.rejectTicket(this.playerId, ticket.id).subscribe({
      next: () => {
        this.toastService.info(`❌ Ordre ${ticket.symbol} ${ticket.side} refusé`);
        this.loadTickets(); // Recharger pour retirer le ticket
        this.scheduledOrderRefreshService.triggerRefresh(); // Rafraîchir la liste des ordres planifiés
        delete this.processingTickets[ticket.id];
      },
      error: (err: any) => {
        console.error('❌ Erreur rejet:', err);
        this.toastService.error('Erreur lors du rejet du ticket');
        delete this.processingTickets[ticket.id];
      }
    });
  }
}
