import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { NgClass } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { WebsocketService } from '../../services/websocket.service';
import { OrderExecutionNotificationService } from '../../services/order-execution-notification.service';
import { MarketAlertNotificationService } from '../../services/market-alert-notification.service';
import { GameMasterEventsService } from '../../services/game-master-events.service';
import { GameMasterService } from '../../services/game-master.service';
import { ToastService } from '../../services/toast.service';
import { NotificationHistoryService } from '../../services/notification-history.service';
import { AuthService } from '../../services/auth.service';
import { ToastContainerComponent } from '../../components/toast-container/toast-container.component';
import { NotificationsPopupComponent } from '../../components/notifications-popup/notifications-popup.component';
import { WalletSummaryComponent } from '../../components/wallet-summary/wallet-summary.component';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, ToastContainerComponent, NotificationsPopupComponent, WalletSummaryComponent],
  template: `
    <app-toast-container></app-toast-container>
    <div class="h-screen w-full flex bg-white text-gray-900 dark:bg-gray-950 dark:text-gray-100">
      <!-- Sidebar -->
      <aside class="w-72 shrink-0 border-r border-gray-200 dark:border-gray-800 bg-gray-50/60 dark:bg-gray-900/60 backdrop-blur relative">
        <div class="h-16 flex items-center justify-between px-4 border-b border-gray-200 dark:border-gray-800">
          <div class="text-xl font-bold tracking-tight">PiBourse</div>
          <div *ngIf="currentUser" class="text-xs text-gray-500 dark:text-gray-400">
            {{ currentUser.username }}
          </div>
        </div>
        <nav class="p-3 space-y-1 pb-20">
          <a routerLink="/player" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-indigo-600 group-hover:text-indigo-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14a5 5 0 1 0 0-10 5 5 0 0 0 0 10Zm-7 7a7 7 0 1 1 14 0H5Z"/></svg>
            <span class="font-medium">Player</span>
          </a>
          <a routerLink="/market" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-emerald-600 group-hover:text-emerald-500" viewBox="0 0 24 24" fill="currentColor"><path d="M3 3h2v18H3V3Zm16 0h2v18h-2V3ZM8 13l3-3 3 3 4-4v9H4v-2h4v-3Z"/></svg>
            <span class="font-medium">Salle de marché</span>
          </a>
          <a routerLink="/orders" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-amber-600 group-hover:text-amber-500" viewBox="0 0 24 24" fill="currentColor"><path d="M7 3h10a2 2 0 0 1 2 2v3H5V5a2 2 0 0 1 2-2Zm-2 8h14v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6Zm4 2v2h6v-2H9Z"/></svg>
            <span class="font-medium">Carnet d'ordre</span>
          </a>
          <a routerLink="/portfolio" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-fuchsia-600 group-hover:text-fuchsia-500" viewBox="0 0 24 24" fill="currentColor"><path d="M3 7a2 2 0 0 1 2-2h3l2-2h4l2 2h3a2 2 0 0 1 2 2v3H3V7Zm0 5h18v5a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-5Z"/></svg>
            <span class="font-medium">Portefeuille</span>
          </a>
          <a routerLink="/credit" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-cyan-600 group-hover:text-cyan-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 1a4 4 0 0 1 4 4v1h2a3 3 0 0 1 3 3v3H3V9a3 3 0 0 1 3-3h2V5a4 4 0 0 1 4-4Zm-9 13h18v2a3 3 0 0 1-3 3h-4v2h-4v-2H6a3 3 0 0 1-3-3v-2Z"/></svg>
            <span class="font-medium">Crédit</span>
          </a>
          <a routerLink="/scheduled-orders" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-violet-600 group-hover:text-violet-500" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1s-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
            <span class="font-medium">Ordres planifiés</span>
          </a>
          <a routerLink="/decision-tickets" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-rose-600 group-hover:text-rose-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 22C6.48 22 2 17.52 2 12S6.48 2 12 2s10 4.48 10 10-4.48 10-10 10zm-1-7h2v2h-2v-2zm0-8h2v6h-2V7z"/></svg>
            <span class="font-medium">🎫 Décisions en attente</span>
          </a>
          <!-- 🎮 Menu Game Master visible seulement pour les meneurs de jeu -->
          <a *ngIf="isGameMaster" routerLink="/game-master" routerLinkActive="!bg-purple-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-purple-600 group-hover:text-purple-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
            <span class="font-medium">🎮 Meneur de Jeu</span>
          </a>
          <a *ngIf="isGameMaster" routerLink="/game-master/price-history" routerLinkActive="!bg-purple-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-purple-600 group-hover:text-purple-500" viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
            <span class="font-medium">📊 Historique des Prix</span>
          </a>
        </nav>

        <!-- Bouton de déconnexion -->
        <div class="absolute bottom-0 left-0 right-0 p-3 border-t border-gray-200 dark:border-gray-800 bg-gray-50/80 dark:bg-gray-900/80">
          <button (click)="logout()" class="w-full flex items-center justify-center gap-2 px-4 py-2 bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400 rounded-lg hover:bg-red-100 dark:hover:bg-red-900/30 transition">
            <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>
            </svg>
            <span class="font-medium">Déconnexion</span>
          </button>
        </div>
      </aside>

      <!-- Content area -->
      <section class="flex-1 flex flex-col min-w-0">
        <!-- Topbar -->
        <header class="h-16 flex items-center justify-between px-4 border-b border-gray-200 dark:border-gray-800 bg-white/60 dark:bg-gray-950/60 backdrop-blur">
          <div class="font-semibold text-lg">Salle de marché boursière</div>
          <div class="flex items-center gap-3">
            <app-wallet-summary [mode]="'button'"></app-wallet-summary>
            
            <!-- 👥 Joueurs Actifs - visible uniquement pour les meneurs de jeu -->
            <button 
              *ngIf="isGameMaster"
              (click)="openActivePlayersModal()" 
              class="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300 hover:bg-purple-200 dark:hover:bg-purple-900/50 transition font-medium">
              <span class="text-lg">👥</span>
              <span class="text-sm">{{ activePlayersCount }} actifs</span>
            </button>
            
            <app-notifications-popup></app-notifications-popup>
            <button (click)="toggleTheme()" class="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-gray-900 text-white dark:bg-gray-100 dark:text-gray-900 hover:opacity-90 transition">
              <svg *ngIf="!isDark" class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 18a6 6 0 1 1 0-12 6 6 0 0 1 0 12Zm0 4v-2m0-16V2m10 10h-2M6 12H4m13.657 6.657-1.414-1.414M7.757 7.757 6.343 6.343m12.728 0-1.414 1.414M7.757 16.243l-1.414 1.414" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <svg *ngIf="isDark" class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z"/></svg>
              <span>{{ isDark ? 'Light' : 'Dark' }} mode</span>
            </button>
          </div>
        </header>
        <main class="flex-1 overflow-auto p-4 bg-gray-50 dark:bg-gray-950">
          <router-outlet />
        </main>
      </section>
    </div>

    <!-- Modal Joueurs Actifs -->
    <div 
      *ngIf="showActivePlayersModal"
      class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      (click)="closeActivePlayersModal()">
      <div 
        class="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-3xl w-full max-h-[80vh] overflow-hidden"
        (click)="$event.stopPropagation()">
        
        <!-- Header -->
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-800 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center text-xl">
              👥
            </div>
            <div>
              <h3 class="text-lg font-bold text-gray-900 dark:text-gray-50">Joueurs Actifs</h3>
              <p class="text-xs text-gray-500 dark:text-gray-400">Connectés dans les 15 dernières minutes</p>
            </div>
          </div>
          <button 
            (click)="closeActivePlayersModal()"
            class="w-8 h-8 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 flex items-center justify-center transition">
            ✕
          </button>
        </div>

        <!-- Content -->
        <div class="p-6 overflow-y-auto max-h-[calc(80vh-80px)]">
          <div *ngIf="activePlayersLoading" class="text-center py-12">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-purple-500 border-t-transparent"></div>
            <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">Chargement...</p>
          </div>

          <div *ngIf="!activePlayersLoading && activePlayers.length === 0" class="text-center py-12">
            <div class="text-4xl mb-3">😴</div>
            <p class="text-gray-500 dark:text-gray-400">Aucun joueur actif pour le moment</p>
          </div>

          <div *ngIf="!activePlayersLoading && activePlayers.length > 0" class="space-y-3">
            <div 
              *ngFor="let player of activePlayers"
              class="rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-800/50 p-4 hover:border-purple-300 dark:hover:border-purple-700 transition">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="font-semibold text-gray-900 dark:text-gray-50">{{ player.username }}</span>
                    <span 
                      class="px-2 py-0.5 rounded text-xs font-medium"
                      [ngClass]="{
                        'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400': player.role === 'ROLE_MENEUR_JEU',
                        'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400': player.role === 'ROLE_PLAYER',
                        'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400': player.role === 'ROLE_ADMIN'
                      }">
                      {{ player.role === 'ROLE_MENEUR_JEU' ? 'Meneur' : player.role === 'ROLE_ADMIN' ? 'Admin' : 'Joueur' }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-600 dark:text-gray-400">{{ player.email }}</p>
                </div>
                <div class="text-right">
                  <div class="flex items-center gap-1 text-xs text-green-600 dark:text-green-400 mb-1">
                    <span class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
                    En ligne
                  </div>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    Dernière activité: {{ formatLastActivity(player.lastActivity) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-800 flex items-center justify-between">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Total: <span class="font-semibold">{{ activePlayers.length }}</span> joueur(s) actif(s)
          </p>
          <button 
            (click)="loadActivePlayers()"
            [disabled]="activePlayersLoading"
            class="px-4 py-2 rounded-lg bg-purple-100 dark:bg-purple-900/30 hover:bg-purple-200 dark:hover:bg-purple-900/50 font-medium text-sm transition disabled:opacity-50">
            🔄 Actualiser
          </button>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class ShellComponent implements OnInit, OnDestroy {
  isDark = false;
  private playerId: number | null = null;
  private subscriptions = new Subscription();
  private wsUnsubs: Array<() => void> = [];
  isGameMaster = false;
  currentUser: any = null;

  // Modal joueurs actifs
  showActivePlayersModal = false;
  activePlayers: any[] = [];
  activePlayersLoading = false;
  activePlayersCount = 0;
  private refreshInterval?: Subscription;

  constructor(
    private websocketService: WebsocketService,
    private notificationService: OrderExecutionNotificationService,
    private marketAlertNotificationService: MarketAlertNotificationService,
    private gameMasterEventsService: GameMasterEventsService,
    private gameMasterService: GameMasterService,
    private toastService: ToastService,
    private notificationHistory: NotificationHistoryService,
    private authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
    const saved = localStorage.getItem('theme');
    this.isDark = saved ? saved === 'dark' : window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.applyTheme();
  }

  ngOnInit(): void {
    // Récupérer l'utilisateur actuel
    this.currentUser = this.authService.getCurrentUser();
    this.playerId = this.authService.getCurrentPlayerId();
    this.isGameMaster = this.authService.isGameMaster();

    console.log('🔍 DEBUG Shell - currentUser:', this.currentUser);
    console.log('🔍 DEBUG Shell - playerId:', this.playerId);
    console.log('🔍 DEBUG Shell - isGameMaster:', this.isGameMaster);

    if (!this.playerId) {
      console.warn('⚠️ Aucun joueur connecté');
      return;
    }

    // Initialiser l'historique des notifications pour ce joueur
    this.notificationHistory.initForPlayer(this.playerId);

    // Demander la permission pour les notifications du navigateur
    this.notificationService.requestNotificationPermission();

    // Initialiser la connexion WebSocket
    this.websocketService.connect().then(() => {
      console.log('🔗 WebSocket connecté depuis Shell');
      // Initialiser l'écoute des notifications pour ce joueur
      this.notificationService.initializeForPlayer(this.playerId!);
      // Initialiser l'écoute des alertes de marché
      this.marketAlertNotificationService.init(this.playerId!);
      
      // 🎮 Initialiser l'écoute des événements Game Master UNIQUEMENT pour les joueurs
      // Les meneurs de jeu ne doivent PAS recevoir leurs propres notifications
      if (!this.isGameMaster) {
        console.log('🎮 Joueur détecté - Abonnement aux événements Game Master');
        this.gameMasterEventsService.startListening(this.playerId!);
      } else {
        console.log('🎮 Meneur de Jeu détecté - PAS d\'abonnement aux événements (vous envoyez, vous ne recevez pas)');
        // Mode debug: permettre au meneur de jeu de recevoir aussi ses propres événements
        const debugReceiveGM = localStorage.getItem('debugReceiveGM') === '1';
        if (debugReceiveGM) {
          console.log('🧪 Debug activé: abonnement aux événements Game Master même en mode meneur de jeu');
          this.gameMasterEventsService.startListening(this.playerId!);
        }
      }

      // 📊 S'abonner aux changements de prix pour ce joueur (joueur et meneur de jeu)
      const priceUnsub = this.websocketService.subscribe(
        `/topic/price-changes/${this.playerId}`,
        (data: any) => this.handlePriceChange(data)
      );
      this.wsUnsubs.push(priceUnsub);

      // 🌪️ S'abonner aux événements de marché pour ce joueur (joueur et meneur de jeu)
      const marketUnsub = this.websocketService.subscribe(
        `/topic/market-events/${this.playerId}`,
        (data: any) => this.handleMarketEvent(data)
      );
      this.wsUnsubs.push(marketUnsub);

      // Écouter les exécutions d'ordres
      const orderSub = this.notificationService.orderExecution$.subscribe(execution => {
        console.log('📊 Ordre reçu dans Shell:', execution);
        this.handleOrderExecution(execution);
      });

      // Écouter les alertes déclenchées
      const alertSub = this.notificationService.alertTriggered$.subscribe(alert => {
        console.log('🚨 Alerte reçue dans Shell:', alert);
        this.handleAlertTriggered(alert);
      });

      // Écouter les tickets de décision (ordres en mode notifyOnly)
      const ticketSub = this.notificationService.decisionTicket$.subscribe(ticket => {
        console.log('🎫 Ticket de décision reçu dans Shell:', ticket);
        this.handleDecisionTicket(ticket);
      });

      this.subscriptions.add(orderSub);
      this.subscriptions.add(alertSub);
      this.subscriptions.add(ticketSub);
    }).catch(err => {
      console.error('❌ Erreur connexion WebSocket:', err);
    });

    // Charger le nombre de joueurs actifs si meneur de jeu
    if (this.isGameMaster) {
      this.loadActivePlayersCount();
      // Rafraîchir toutes les 30 secondes
      this.refreshInterval = interval(30000).subscribe(() => {
        this.loadActivePlayersCount();
      });
    }
  }

  /**
   * Gère les notifications d'exécution d'ordres
   */
  private handleOrderExecution(execution: any): void {
    // Afficher un toast/notification visuelle avec navigation vers les ordres
    this.toastService.success(
      `✅ Ordre exécuté: ${execution.symbol} ${execution.side} @ ${execution.executedPrice}€`,
      5000,
      true,
      { symbol: execution.symbol, route: '/orders' }
    );
    
    // Sauvegarder dans l'historique
    this.notificationHistory.addNotification(
      'ORDER_EXECUTED',
      'Ordre exécuté',
      `${execution.symbol} ${execution.side} @ ${execution.executedPrice}€`,
      { orderId: execution.executedOrderId, execution }
    );
  }

  /**
   * Gère les notifications d'alertes déclenchées
   */
  private handleAlertTriggered(alert: any): void {
    // Afficher une notification urgente (alerte prix atteint)
    const minPrice = alert.minPrice ? alert.minPrice.toFixed(2) : '∞';
    const maxPrice = alert.maxPrice ? alert.maxPrice.toFixed(2) : '∞';
    
    const message = `${alert.symbol} @ ${alert.currentPrice?.toFixed(2)}€ dans [${minPrice}, ${maxPrice}]`;
    
    this.toastService.warning(
      `🚨 ALERTE: ${message}`,
      0, // Durée infinie (l'utilisateur doit fermer manuellement)
      true,
      { symbol: alert.symbol, route: '/orders' }
    );
    
    // Sauvegarder dans l'historique
    this.notificationHistory.addNotification(
      'ALERT_TRIGGERED',
      '🚨 Alerte de prix',
      message,
      { alert }
    );
  }

  /**
   * Gère les tickets de décision (ordres en mode notifyOnly)
   */
  private handleDecisionTicket(ticket: any): void {
    const message = ticket.reason === 'APPROACHING_MIN'
      ? `${ticket.symbol} approche ${ticket.foundPrice}€`
      : `${ticket.symbol} ${ticket.side} @ ${ticket.foundPrice}€`;
    
    this.toastService.info(
      `🎫 ${message}`,
      0,
      true,
      { route: '/decision-tickets' }
    );
    
    // Sauvegarder dans l'historique avec ticketId pour navigation
    this.notificationHistory.addNotification(
      'DECISION_TICKET',
      '🎫 Ticket de décision',
      message,
      { ticketId: ticket.id, ticket }
    );
  }

  /**
   * Affiche un toast/notification visuelle
   */
  private showToast(message: string, type: 'success' | 'warning' | 'error'): void {
    switch (type) {
      case 'success':
        this.toastService.success(message);
        break;
      case 'warning':
        this.toastService.warning(message);
        break;
      case 'error':
        this.toastService.error(message);
        break;
    }
  }

  toggleTheme() {
    this.isDark = !this.isDark;
    localStorage.setItem('theme', this.isDark ? 'dark' : 'light');
    this.applyTheme();
  }

  /**
   * Méthode de test pour créer une notification manuellement
   */
  testNotification(): void {
    console.log('🧪 TEST: Création notification manuelle');
    this.notificationHistory.addNotification(
      'ALERT_TRIGGERED',
      'Test de notification',
      'Ceci est une notification de test créée manuellement',
      { symbol: 'TEST', currentPrice: 123.45 }
    );
    this.toastService.success('✅ Notification de test créée !');
  }

  logout(): void {
    this.authService.logout();
    this.websocketService.disconnect();
    this.notificationHistory.reset(); // Vider les notifications du joueur actuel
    this.router.navigate(['/login']);
  }

  loadActivePlayersCount(): void {
    this.gameMasterService.getActivePlayers().subscribe({
      next: (response) => {
        if (Array.isArray(response)) {
          this.activePlayersCount = response.length;
        } else if (response && typeof response === 'object') {
          const count = response.count;
          const players = Array.isArray(response.players) ? response.players.length : 0;
          this.activePlayersCount = count && count > 0 ? count : players;
        } else {
          this.activePlayersCount = 0;
        }
      },
      error: (err) => {
        console.error('❌ Erreur chargement nombre joueurs actifs:', err);
      }
    });
  }

  loadActivePlayers(): void {
    this.activePlayersLoading = true;
    this.gameMasterService.getActivePlayers().subscribe({
      next: (response) => {
        if (Array.isArray(response)) {
          this.activePlayers = response;
        } else if (response && typeof response === 'object') {
          this.activePlayers = response.players || [];
        } else {
          this.activePlayers = [];
        }

        const countFromArray = Array.isArray(this.activePlayers) ? this.activePlayers.length : 0;
        const countFromResponse = response && typeof response === 'object' && response.count ? response.count : 0;
        this.activePlayersCount = countFromResponse && countFromResponse > 0 ? countFromResponse : countFromArray;
        this.activePlayersLoading = false;
      },
      error: (err) => {
        console.error('❌ Erreur chargement joueurs actifs:', err);
        this.activePlayersLoading = false;
        this.toastService.error('Erreur chargement des joueurs actifs');
      }
    });
  }

  openActivePlayersModal(): void {
    this.showActivePlayersModal = true;
    this.loadActivePlayers();
  }

  closeActivePlayersModal(): void {
    this.showActivePlayersModal = false;
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

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.marketAlertNotificationService.destroy();
    this.gameMasterEventsService.stopListening();
    // Désabonner des topics WebSocket spécifiques
    for (const unsub of this.wsUnsubs) {
      try { unsub(); } catch {}
    }
    this.websocketService.disconnect();
    if (this.refreshInterval) {
      this.refreshInterval.unsubscribe();
    }
  }

  private handlePriceChange(data: any): void {
    try {
      const percent = typeof data.changePercent === 'number'
        ? data.changePercent
        : parseFloat(data.changePercent);
      const icon = percent >= 0 ? '📈' : '📉';
      const color: 'success' | 'warning' | 'error' | 'info' = percent >= 0 ? 'success' : 'warning';
      const msg = `${icon} ${data.symbol}: ${data.oldPrice}€ → ${data.newPrice}€ (${percent.toFixed(2)}%)`;
      if (color === 'success') this.toastService.success(msg, 7000);
      else this.toastService.warning(msg, 7000);

      // Enregistrer dans l'historique pour le panneau Notifications
      this.notificationHistory.addNotification(
        'PRICE_CHANGE',
        'Changement de prix',
        msg,
        {
          symbol: data.symbol,
          oldPrice: data.oldPrice,
          newPrice: data.newPrice,
          changePercent: percent
        }
      );
    } catch (e) {
      console.log('Erreur affichage price change:', e, data);
    }
  }

  private handleMarketEvent(data: any): void {
    try {
      const icons: Record<string, string> = {
        CRASH: '📉',
        BULL_RUN: '📈',
        VOLATILITY: '⚡',
        MANIPULATION: '🎯'
      };
      const icon = icons[data.eventType] || '📢';
      const title = `${icon} ${data.eventName || data.eventType}`;
      const message = data.description || 'Événement de marché';
      this.toastService.info(`${title} — ${message}`, 10000);

      // Enregistrer dans l'historique pour le panneau Notifications
      this.notificationHistory.addNotification(
        'MARKET_EVENT',
        title,
        message,
        {
          eventType: data.eventType,
          eventName: data.eventName,
          description: data.description,
          affectedSymbols: data.affectedSymbols
        }
      );
    } catch (e) {
      console.log('Erreur affichage market event:', e, data);
    }
  }

  private applyTheme() {
    const root = document.documentElement;
    if (this.isDark) root.classList.add('dark');
    else root.classList.remove('dark');
  }
}
