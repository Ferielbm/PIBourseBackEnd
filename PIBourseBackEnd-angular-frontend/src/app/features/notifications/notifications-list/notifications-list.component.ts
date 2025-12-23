import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NotificationHistoryService } from '../../../services/notification-history.service';
import { Notification } from '../../../models/notification.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notifications-container">
      <div class="header">
        <h2>🔔 Notifications</h2>
        <div class="header-actions">
          <button 
            *ngIf="unreadCount > 0" 
            (click)="markAllAsRead()"
            class="btn-mark-read">
            Tout marquer comme lu
          </button>
          <button 
            (click)="clearAll()"
            class="btn-clear">
            Effacer tout
          </button>
        </div>
      </div>

      <div *ngIf="notifications.length === 0" class="empty-state">
        <svg class="icon" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
        </svg>
        <p>Aucune notification</p>
      </div>

      <div class="notifications-list">
        <div 
          *ngFor="let notif of notifications" 
          class="notification-item"
          [class.unread]="!notif.read"
          (click)="handleNotificationClick(notif)">
          
          <div class="notif-icon" [class]="getIconClass(notif.type)">
            {{ getIcon(notif.type) }}
          </div>

          <div class="notif-content">
            <div class="notif-header">
              <span class="notif-title">{{ notif.title }}</span>
              <span class="notif-time">{{ formatTime(notif.createdAt) }}</span>
            </div>
            <p class="notif-message">{{ notif.message }}</p>
          </div>

          <div class="notif-actions">
            <button 
              *ngIf="!notif.read"
              (click)="markAsRead(notif.id, $event)"
              class="btn-mark"
              title="Marquer comme lu">
              ✓
            </button>
            <button 
              (click)="deleteNotif(notif.id, $event)"
              class="btn-delete"
              title="Supprimer">
              ✕
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .notifications-container {
      padding: 20px;
      max-width: 900px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      flex-wrap: wrap;
      gap: 12px;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
    }

    :host-context(.dark) h2 {
      color: #f9fafb;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }

    .btn-mark-read, .btn-clear {
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-mark-read {
      background: #3b82f6;
      color: white;
    }

    .btn-mark-read:hover {
      background: #2563eb;
    }

    :host-context(.dark) .btn-mark-read {
      background: #2563eb;
    }

    :host-context(.dark) .btn-mark-read:hover {
      background: #1d4ed8;
    }

    .btn-clear {
      background: #ef4444;
      color: white;
    }

    .btn-clear:hover {
      background: #dc2626;
    }

    :host-context(.dark) .btn-clear {
      background: #dc2626;
    }

    :host-context(.dark) .btn-clear:hover {
      background: #b91c1c;
    }

    .empty-state {
      text-align: center;
      padding: 80px 20px;
      color: #6b7280;
    }

    :host-context(.dark) .empty-state {
      color: #9ca3af;
    }

    .empty-state .icon {
      width: 64px;
      height: 64px;
      margin: 0 auto 16px;
      opacity: 0.3;
    }

    .empty-state p {
      font-size: 16px;
      margin: 0;
    }

    .notifications-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .notification-item {
      display: flex;
      gap: 12px;
      padding: 16px;
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.2s;
    }

    :host-context(.dark) .notification-item {
      background: #1f2937;
      border-color: #374151;
    }

    .notification-item:hover {
      border-color: #3b82f6;
      box-shadow: 0 4px 6px rgba(59, 130, 246, 0.1);
    }

    :host-context(.dark) .notification-item:hover {
      border-color: #60a5fa;
      box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2);
    }

    .notification-item.unread {
      background: #eff6ff;
      border-color: #3b82f6;
    }

    :host-context(.dark) .notification-item.unread {
      background: #1e3a8a;
      border-color: #3b82f6;
    }

    .notif-icon {
      flex-shrink: 0;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      font-size: 20px;
    }

    .notif-icon.executed {
      background: #d1fae5;
      color: #065f46;
    }

    :host-context(.dark) .notif-icon.executed {
      background: #064e3b;
      color: #6ee7b7;
    }

    .notif-icon.ticket {
      background: #fef3c7;
      color: #92400e;
    }

    :host-context(.dark) .notif-icon.ticket {
      background: #78350f;
      color: #fde68a;
    }

    .notif-icon.alert {
      background: #fee2e2;
      color: #991b1b;
    }

    :host-context(.dark) .notif-icon.alert {
      background: #7f1d1d;
      color: #fecaca;
    }

    .notif-content {
      flex: 1;
      min-width: 0;
    }

    .notif-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      gap: 8px;
    }

    .notif-title {
      font-weight: 600;
      font-size: 14px;
      color: #1f2937;
    }

    :host-context(.dark) .notif-title {
      color: #f9fafb;
    }

    .notif-time {
      font-size: 12px;
      color: #6b7280;
      white-space: nowrap;
    }

    :host-context(.dark) .notif-time {
      color: #9ca3af;
    }

    .notif-message {
      margin: 0;
      font-size: 14px;
      color: #4b5563;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    :host-context(.dark) .notif-message {
      color: #d1d5db;
    }

    .notif-actions {
      display: flex;
      gap: 4px;
      flex-shrink: 0;
    }

    .btn-mark, .btn-delete {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      border-radius: 4px;
      background: transparent;
      color: #6b7280;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 14px;
    }

    .btn-mark:hover {
      background: #10b981;
      color: white;
    }

    .btn-delete:hover {
      background: #ef4444;
      color: white;
    }

    @media (max-width: 640px) {
      .notifications-container {
        padding: 12px;
      }

      .header {
        flex-direction: column;
        align-items: flex-start;
      }

      .header-actions {
        width: 100%;
        justify-content: stretch;
      }

      .btn-mark-read, .btn-clear {
        flex: 1;
      }

      .notification-item {
        padding: 12px;
      }

      .notif-icon {
        width: 32px;
        height: 32px;
        font-size: 16px;
      }
    }
  `]
})
export class NotificationsListComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount = 0;
  private subscription = new Subscription();

  constructor(
    private notificationHistory: NotificationHistoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const sub = this.notificationHistory.notifications$.subscribe(notifs => {
      this.notifications = notifs;
      this.unreadCount = notifs.filter(n => !n.read).length;
    });
    this.subscription.add(sub);
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  handleNotificationClick(notif: Notification): void {
    this.markAsRead(notif.id);

    // Navigation selon le type
    if (notif.type === 'DECISION_TICKET') {
      this.router.navigate(['/decision-tickets']);
    } 
    else if (notif.type === 'ALERT_TRIGGERED') {
      // Naviguer vers les ordres avec le symbole de l'alerte
      const symbol = notif.data?.alert?.symbol || notif.data?.symbol;
      if (symbol) {
        this.router.navigate(['/orders'], { queryParams: { symbol } });
      } else {
        this.router.navigate(['/alert-market']);
      }
    }
    else if (notif.type === 'ORDER_EXECUTED') {
      // Naviguer vers les ordres avec le symbole de l'ordre exécuté
      const symbol = notif.data?.execution?.symbol || notif.data?.symbol;
      if (symbol) {
        this.router.navigate(['/orders'], { queryParams: { symbol } });
      } else {
        this.router.navigate(['/orders']);
      }
    }
  }

  markAsRead(id: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.notificationHistory.markAsRead(id);
  }

  markAllAsRead(): void {
    this.notificationHistory.markAllAsRead();
  }

  deleteNotif(id: number, event: Event): void {
    event.stopPropagation();
    if (confirm('Supprimer cette notification ?')) {
      this.notificationHistory.deleteNotification(id);
    }
  }

  clearAll(): void {
    if (confirm('Effacer toutes les notifications ?')) {
      this.notificationHistory.clearAll();
    }
  }

  getIcon(type: string): string {
    switch (type) {
      case 'ORDER_EXECUTED': return '✓';
      case 'DECISION_TICKET': return '🎫';
      case 'ALERT_TRIGGERED': return '🚨';
      default: return '📬';
    }
  }

  getIconClass(type: string): string {
    switch (type) {
      case 'ORDER_EXECUTED': return 'executed';
      case 'DECISION_TICKET': return 'ticket';
      case 'ALERT_TRIGGERED': return 'alert';
      default: return '';
    }
  }

  formatTime(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - new Date(date).getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes}min`;
    if (hours < 24) return `Il y a ${hours}h`;
    if (days < 7) return `Il y a ${days}j`;
    
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: '2-digit'
    });
  }
}
