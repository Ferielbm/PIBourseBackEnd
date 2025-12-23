import { Component, OnDestroy, OnInit, HostListener, TemplateRef, ViewChild, ViewContainerRef, EmbeddedViewRef, ApplicationRef, Renderer2 } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationHistoryService } from '../../services/notification-history.service';
import { Notification } from '../../models/notification.model';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notifications-popup',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative" (click)="togglePopup($event)">
      <!-- Bell Icon -->
      <button type="button" class="relative flex items-center justify-center w-9 h-9 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition" [attr.aria-expanded]="open">
        <svg class="h-5 w-5 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5S9.5 3.17 9.5 4v.68C6.63 5.36 5 7.92 5 11v5l-2 2v1h18v-1l-2-2z"/>
        </svg>
        <span *ngIf="unreadCount > 0" class="absolute -top-1 -right-1 bg-red-600 text-white text-[10px] font-semibold rounded-full px-1.5 py-0.5 shadow">{{ unreadCount }}</span>
      </button>
    </div>

    <!-- Popup template rendu via portal (attach to body) -->
    <ng-template #popupTemplate>
      <div class="fixed inset-0" style="z-index: 999998;" (click)="close()">
        <div class="fixed right-4 top-16 w-[28rem] sm:w-[32rem] max-h-[calc(100vh-5rem)] overflow-hidden rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 shadow-2xl ring-1 ring-black/5" style="z-index: 999999;" (click)="$event.stopPropagation()">
          <div class="flex items-center justify-between px-4 py-3 bg-gradient-to-r from-indigo-50 to-white dark:from-gray-800 dark:to-gray-900 border-b border-gray-100 dark:border-gray-800">
            <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">Notifications</h3>
            <div class="flex gap-2">
              <button (click)="goToNotifications($event)" class="text-xs px-2 py-1 rounded bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300 hover:bg-blue-200 dark:hover:bg-blue-800">Voir tout</button>
              <button *ngIf="unreadCount > 0" (click)="markAllAsRead($event)" class="text-xs px-2 py-1 rounded bg-indigo-600 text-white hover:bg-indigo-500">Tout lire</button>
              <button (click)="clearAll($event)" class="text-xs px-2 py-1 rounded bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-200 hover:bg-gray-300 dark:hover:bg-gray-600">Vider</button>
            </div>
          </div>
          <div *ngIf="notifications.length === 0" class="p-8 text-center text-sm text-gray-500 dark:text-gray-400">
            <svg class="h-16 w-16 mx-auto mb-3 text-gray-300 dark:text-gray-600" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5S9.5 3.17 9.5 4v.68C6.63 5.36 5 7.92 5 11v5l-2 2v1h18v-1l-2-2z"/>
            </svg>
            <p class="font-medium text-gray-600 dark:text-gray-400">Aucune notification</p>
            <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">Vous serez notifié ici des ordres exécutés et alertes</p>
          </div>
          <div class="flex flex-col divide-y divide-gray-100 dark:divide-gray-800 overflow-y-auto max-h-[calc(100vh-12rem)] bg-white dark:bg-gray-900">
            <div *ngFor="let n of notifications"
                 [class]="'group px-4 py-3.5 flex gap-3 hover:bg-gradient-to-r hover:from-indigo-50 hover:to-blue-50 dark:hover:from-gray-800 dark:hover:to-gray-800 cursor-pointer relative transition-all duration-200' + (!n.read ? ' bg-gradient-to-r from-blue-50 via-indigo-50 to-purple-50 dark:from-blue-900/10 dark:via-indigo-900/10 dark:to-purple-900/10 border-l-4 border-indigo-500' : '')"
                 (click)="onNotificationClick(n)">
              <!-- Icône avec animation -->
              <div class="flex-shrink-0 relative">
                <div class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl shadow-lg" [ngClass]="getIconWrapperClass(n.type, n.read)">
                  <span class="relative z-10">{{ getIcon(n.type) }}</span>
                </div>
                <!-- Badge pulsant pour les non-lus -->
                <span *ngIf="!n.read" class="absolute -top-1 -right-1 flex h-3 w-3">
                  <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-3 w-3 bg-indigo-600"></span>
                </span>
              </div>
              
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between mb-1.5">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-semibold text-gray-600 dark:text-gray-300" [ngClass]="{'text-indigo-600 dark:text-indigo-400': !n.read}">
                      {{ formatTime(n.createdAt) }}
                    </span>
                    <span *ngIf="!n.read" class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-600 text-white animate-pulse">
                      NOUVEAU
                    </span>
                  </div>
                  <button *ngIf="!n.read" (click)="markAsRead(n.id, $event)" class="text-[10px] px-2.5 py-1 rounded-full bg-gradient-to-r from-green-500 to-emerald-500 text-white hover:from-green-600 hover:to-emerald-600 shadow-md hover:shadow-lg font-semibold transition-all transform hover:scale-105">
                    ✓ Marquer lu
                  </button>
                </div>
                
                <!-- Titre avec badge de type -->
                <div class="flex items-start gap-2 mb-1">
                  <p class="text-sm text-gray-900 dark:text-gray-50 font-semibold leading-tight flex-1" [ngClass]="{'text-indigo-900 dark:text-indigo-100': !n.read}">
                    {{ n.title }}
                  </p>
                  <span class="text-[10px] font-bold px-2 py-0.5 rounded-full" [ngClass]="getTypeBadgeClass(n.type)">
                    {{ getTypeLabel(n.type) }}
                  </span>
                </div>
                
                <!-- Message -->
                <p class="text-xs text-gray-700 dark:text-gray-300 leading-relaxed mb-2" [ngClass]="{'font-medium': !n.read}">
                  {{ n.message }}
                </p>
                
                <!-- Tags de données avec icônes -->
                <div *ngIf="n.data && (n.data.symbol || n.data.currentPrice || n.data.execution)" class="flex flex-wrap gap-1.5">
                  <span *ngIf="n.data.symbol" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200 border border-blue-200 dark:border-blue-800">
                    <svg class="w-3 h-3" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M3 3h18v18H3V3zm16 16V5H5v14h14z"/>
                    </svg>
                    {{ n.data.symbol }}
                  </span>
                  <span *ngIf="n.data.currentPrice" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-200 border border-emerald-200 dark:border-emerald-800">
                    <svg class="w-3 h-3" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
                    </svg>
                    {{ n.data.currentPrice }}€
                  </span>
                  <span *ngIf="n.data.execution?.side" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold" [ngClass]="n.data.execution.side === 'BUY' ? 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200 border border-green-200 dark:border-green-800' : 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200 border border-red-200 dark:border-red-800'">
                    {{ n.data.execution.side === 'BUY' ? '📈 ACHAT' : '📉 VENTE' }}
                  </span>
                  <span *ngIf="n.data.execution?.executedPrice" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-200 border border-purple-200 dark:border-purple-800">
                    Prix: {{ n.data.execution.executedPrice }}€
                  </span>
                </div>
              </div>
              
              <!-- Bouton supprimer amélioré -->
              <button (click)="delete(n.id, $event)" class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-all text-gray-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg w-7 h-7 flex items-center justify-center hover:scale-110 transform">
                <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </ng-template>
  `,
  styles: [``]
})
export class NotificationsPopupComponent implements OnInit, OnDestroy {
  @ViewChild('popupTemplate', { read: TemplateRef }) popupTemplate!: TemplateRef<any>;

  notifications: Notification[] = [];
  unreadCount = 0;
  open = false;
  private sub = new Subscription();

  private embeddedView?: EmbeddedViewRef<any>;

  constructor(
    private history: NotificationHistoryService,
    private router: Router,
    private appRef: ApplicationRef,
    private viewContainerRef: ViewContainerRef,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    console.log('🔔 NotificationsPopup: Initialisation');
    this.sub.add(this.history.notifications$.subscribe(list => {
      console.log('🔔 NotificationsPopup: Notifications reçues:', list.length);
      this.notifications = list;
      this.unreadCount = list.filter(n => !n.read).length;
      console.log('🔔 NotificationsPopup: Non lues:', this.unreadCount);
    }));
  }

  togglePopup(event: Event) {
    event.stopPropagation();
    this.open = !this.open;
    console.log('🔔 NotificationsPopup: Toggle popup -', this.open ? 'OUVERT' : 'FERMÉ');
    console.log('🔔 NotificationsPopup: Nombre de notifications:', this.notifications.length);
    if (this.open) {
      this.attachPortal();
    } else {
      this.detachPortal();
    }
  }

  close() {
    this.open = false;
    this.detachPortal();
  }

  onNotificationClick(n: Notification) {
    this.markAsRead(n.id);
    // Redirection vers la page appropriée selon le type
    if (n.type === 'DECISION_TICKET') {
      this.router.navigate(['/decision-tickets']);
    } else {
      this.router.navigate(['/notifications']);
    }
    this.open = false;
  }

  markAsRead(id: number, event?: Event) {
    if (event) event.stopPropagation();
    this.history.markAsRead(id);
  }

  markAllAsRead(event?: Event) {
    if (event) event.stopPropagation();
    this.history.markAllAsRead();
  }

  clearAll(event?: Event) {
    if (event) event.stopPropagation();
    if (confirm('Effacer toutes les notifications ?')) {
      this.history.clearAll();
    }
  }

  goToNotifications(event?: Event) {
    if (event) event.stopPropagation();
    this.open = false;
    this.router.navigate(['/notifications']);
  }

  delete(id: number, event: Event) {
    event.stopPropagation();
    this.history.deleteNotification(id);
  }

  getIcon(type: string): string {
    switch (type) {
      case 'ORDER_EXECUTED': return '✓';
      case 'DECISION_TICKET': return '🎫';
      case 'ALERT_TRIGGERED': return '🚨';
      case 'PRICE_CHANGE': return '💱';
      case 'MARKET_EVENT': return '📢';
      case 'GAME_MASTER_EVENT': return '🎮';
      default: return '📬';
    }
  }

  getIconWrapperClass(type: string, read: boolean): string {
    const opacity = read ? 'opacity-70' : 'shadow-lg';
    switch (type) {
      case 'ORDER_EXECUTED': return opacity + ' bg-gradient-to-br from-emerald-100 via-green-100 to-emerald-200 text-emerald-700 dark:from-emerald-900/50 dark:via-emerald-800/50 dark:to-emerald-900/50 dark:text-emerald-300';
      case 'DECISION_TICKET': return opacity + ' bg-gradient-to-br from-amber-100 via-yellow-100 to-orange-200 text-amber-700 dark:from-amber-900/50 dark:via-yellow-900/50 dark:to-orange-800/50 dark:text-amber-300';
      case 'ALERT_TRIGGERED': return opacity + ' bg-gradient-to-br from-rose-100 via-red-100 to-red-200 text-rose-700 dark:from-rose-900/50 dark:via-red-900/50 dark:to-red-800/50 dark:text-rose-300';
      case 'PRICE_CHANGE': return opacity + ' bg-gradient-to-br from-blue-100 via-indigo-100 to-blue-200 text-blue-700 dark:from-blue-900/40 dark:via-indigo-900/40 dark:to-blue-900/40 dark:text-blue-300';
      case 'MARKET_EVENT': return opacity + ' bg-gradient-to-br from-sky-100 via-cyan-100 to-blue-200 text-sky-700 dark:from-sky-900/40 dark:via-cyan-900/40 dark:to-blue-900/40 dark:text-sky-300';
      case 'GAME_MASTER_EVENT': return opacity + ' bg-gradient-to-br from-purple-100 via-fuchsia-100 to-pink-200 text-purple-700 dark:from-purple-900/50 dark:via-fuchsia-900/50 dark:to-pink-800/50 dark:text-purple-300';
      default: return opacity + ' bg-gradient-to-br from-gray-100 to-gray-200 text-gray-600 dark:from-gray-700 dark:to-gray-600 dark:text-gray-300';
    }
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'ORDER_EXECUTED': return 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-200 border border-emerald-300 dark:border-emerald-700';
      case 'DECISION_TICKET': return 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-200 border border-amber-300 dark:border-amber-700';
      case 'ALERT_TRIGGERED': return 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-200 border border-rose-300 dark:border-rose-700';
      case 'PRICE_CHANGE': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200 border border-blue-300 dark:border-blue-700';
      case 'MARKET_EVENT': return 'bg-sky-100 text-sky-800 dark:bg-sky-900/40 dark:text-sky-200 border border-sky-300 dark:border-sky-700';
      case 'GAME_MASTER_EVENT': return 'bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-200 border border-purple-300 dark:border-purple-700';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/40 dark:text-gray-200 border border-gray-300 dark:border-gray-700';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'ORDER_EXECUTED': return 'ORDRE';
      case 'DECISION_TICKET': return 'TICKET';
      case 'ALERT_TRIGGERED': return 'ALERTE';
      case 'PRICE_CHANGE': return 'PRIX';
      case 'MARKET_EVENT': return 'MARCHÉ';
      case 'GAME_MASTER_EVENT': return 'ÉVÉNEMENT';
      default: return 'INFO';
    }
  }

  formatTime(date: Date): string {
    const d = new Date(date);
    const diff = Date.now() - d.getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1) return "À l'instant";
    if (m < 60) return `Il y a ${m}min`;
    const h = Math.floor(m / 60);
    if (h < 24) return `Il y a ${h}h`;
    const days = Math.floor(h / 24);
    if (days < 7) return `Il y a ${days}j`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' });
  }

  @HostListener('document:click') onDocClick() {
    if (this.open) this.close();
  }

  @HostListener('document:keydown.escape') onEsc() { this.open = false; }

  private attachPortal() {
    if (!this.popupTemplate || this.embeddedView) return;
    this.embeddedView = this.popupTemplate.createEmbeddedView({});
    this.appRef.attachView(this.embeddedView);
    // append root nodes to body
    for (const node of this.embeddedView.rootNodes) {
      this.renderer.appendChild(document.body, node);
    }
  }

  private detachPortal() {
    if (!this.embeddedView) return;
    // remove nodes from body
    for (const node of this.embeddedView.rootNodes) {
      if (node.parentNode) node.parentNode.removeChild(node);
    }
    this.appRef.detachView(this.embeddedView);
    this.embeddedView.destroy();
    this.embeddedView = undefined;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    this.detachPortal();
  }
}
