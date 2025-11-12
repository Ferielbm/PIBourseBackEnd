import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, Subscription } from 'rxjs';

export interface TimeStatus {
  active: boolean;  // Correspond à "active" dans votre controller
  currentGameTime: string;
  compressionInfo: string;
}

@Injectable({
  providedIn: 'root'
})
export class TimeAcceleratorService {
  private baseUrl = 'http://localhost:8080/api/market/time'; // URLs du MarketDataController
  
  private currentGameTime$ = new BehaviorSubject<Date | null>(null);
  private isActive$ = new BehaviorSubject<boolean>(false);
  private pollSubscription?: Subscription;

  constructor(private http: HttpClient) {
    // Démarrer le polling automatique pour obtenir le temps actuel
    this.startPolling();
  }

  /**
   * Obtenir le temps de jeu actuel (Observable)
   */
  getCurrentGameTime(): Observable<Date | null> {
    return this.currentGameTime$.asObservable();
  }

  /**
   * Vérifier si l'accélération est active
   */
  isAccelerationActive(): Observable<boolean> {
    return this.isActive$.asObservable();
  }

  /**
   * Démarrer l'accélération du temps sur le serveur
   */
  startAcceleration(): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/start`, {});
  }

  /**
   * Arrêter l'accélération du temps sur le serveur
   */
  stopAcceleration(): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/stop`, {});
  }

  /**
   * Réinitialiser le temps au début de l'année
   */
  resetAcceleration(): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/reset`, {});
  }

  /**
   * Obtenir le statut actuel du temps (appel unique)
   */
  fetchCurrentStatus(): Observable<TimeStatus> {
    return this.http.get<TimeStatus>(`${this.baseUrl}/status`);
  }

  /**
   * Démarrer le polling pour mettre à jour le temps toutes les secondes
   */
  private startPolling(): void {
    // Appel immédiat
    this.updateTimeStatus();

    // Puis toutes les 1 seconde
    this.pollSubscription = interval(1000).subscribe(() => {
      this.updateTimeStatus();
    });
  }

  /**
   * Mettre à jour le statut du temps
   */
  private updateTimeStatus(): void {
    this.fetchCurrentStatus().subscribe({
      next: (status) => {
        this.currentGameTime$.next(new Date(status.currentGameTime));
        this.isActive$.next(status.active);
      },
      error: (err) => {
        // Si le backend n'est pas disponible, on garde la dernière valeur
        console.warn('[TimeAccelerator] Backend not available:', err.message);
      }
    });
  }

  /**
   * Arrêter le polling (quand le composant est détruit)
   */
  stopPolling(): void {
    this.pollSubscription?.unsubscribe();
  }
}
