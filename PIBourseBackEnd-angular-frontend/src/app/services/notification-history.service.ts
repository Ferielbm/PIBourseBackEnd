import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationHistoryService {
  private readonly STORAGE_KEY_PREFIX = 'pibourse_notifications_';
  private notifications: Notification[] = [];
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  private nextId = 1;
  private currentPlayerId: number | null = null;

  notifications$ = this.notificationsSubject.asObservable();

  constructor() {
    // Ne pas charger automatiquement - attendre que le playerId soit défini
  }

  /**
   * Initialiser le service pour un joueur spécifique
   */
  initForPlayer(playerId: number): void {
    console.log('📢 NotificationHistoryService: Init pour player', playerId);
    if (this.currentPlayerId === playerId) {
      console.log('📢 NotificationHistoryService: Déjà initialisé pour ce joueur');
      return; // Déjà initialisé pour ce joueur
    }
    this.currentPlayerId = playerId;
    this.loadFromStorage();
    console.log('📢 NotificationHistoryService: Notifications chargées:', this.notifications.length);
  }

  /**
   * Réinitialiser le service (au logout)
   */
  reset(): void {
    this.currentPlayerId = null;
    this.notifications = [];
    this.notificationsSubject.next([]);
  }

  private getStorageKey(): string {
    if (!this.currentPlayerId) {
      throw new Error('NotificationHistoryService: playerId non défini');
    }
    return this.STORAGE_KEY_PREFIX + this.currentPlayerId;
  }

  private loadFromStorage(): void {
    const stored = localStorage.getItem(this.getStorageKey());
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        this.notifications = parsed.map((n: any) => ({
          ...n,
          createdAt: new Date(n.createdAt)
        }));
        this.nextId = Math.max(...this.notifications.map(n => n.id), 0) + 1;
        this.notificationsSubject.next(this.notifications);
      } catch (e) {
        console.error('Erreur chargement notifications:', e);
      }
    }
  }

  private saveToStorage(): void {
    localStorage.setItem(this.getStorageKey(), JSON.stringify(this.notifications));
    this.notificationsSubject.next(this.notifications);
  }

  addNotification(
    type: 'ORDER_EXECUTED' | 'DECISION_TICKET' | 'ALERT_TRIGGERED' | 'PRICE_CHANGE' | 'MARKET_EVENT' | 'GAME_MASTER_EVENT',
    title: string,
    message: string,
    data?: any
  ): Notification {
    console.log('📢 NotificationHistoryService: NOUVELLE NOTIFICATION', { type, title, message });
    const notification: Notification = {
      id: this.nextId++,
      type,
      title,
      message,
      read: false,
      createdAt: new Date(),
      data
    };

    this.notifications.unshift(notification); // Ajouter au début
    console.log('📢 NotificationHistoryService: Total notifications:', this.notifications.length);
    
    // Garder seulement les 100 dernières notifications
    if (this.notifications.length > 100) {
      this.notifications = this.notifications.slice(0, 100);
    }

    this.saveToStorage();
    return notification;
  }

  markAsRead(id: number): void {
    const notification = this.notifications.find(n => n.id === id);
    if (notification) {
      notification.read = true;
      this.saveToStorage();
    }
  }

  markAllAsRead(): void {
    this.notifications.forEach(n => n.read = true);
    this.saveToStorage();
  }

  deleteNotification(id: number): void {
    this.notifications = this.notifications.filter(n => n.id !== id);
    this.saveToStorage();
  }

  clearAll(): void {
    this.notifications = [];
    this.saveToStorage();
  }

  getUnreadCount(): Observable<number> {
    return new Observable(observer => {
      this.notifications$.subscribe(notifications => {
        observer.next(notifications.filter(n => !n.read).length);
      });
    });
  }

  getAll(): Notification[] {
    return this.notifications;
  }
}
