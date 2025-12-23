import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, Subscription, takeUntil, Subject, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TokenStorageService } from '../player/token-storage.service';
import { AuthService } from '../player/auth.service';
import { SimulationTimeService } from './simulation-time.service';

export interface TimeStatus {
  active: boolean;  // Correspond à "active" dans votre controller
  currentGameTime: string | Date | null;
  compressionInfo?: string;
  // Optionnel : ratio numérique fourni par le backend (game seconds per real second)
  currentRatio?: number;
  defaultPlayerRatio?: number;
  maxGameMasterRatio?: number;
  isGameMasterRatio?: boolean;
  simulationPeriod?: string;
  historicalDataAvailable?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TimeAcceleratorService {
  private baseUrl = 'http://localhost:8080/api/market/time'; // URLs du MarketDataController
  private timeTravelBase = 'http://localhost:8080/api/time-travel';
  
  private currentGameTime$ = new BehaviorSubject<Date | null>(null);
  private isActive$ = new BehaviorSubject<boolean>(false);
  private compressionInfo$ = new BehaviorSubject<string>('');
  private compressionRatio$ = new BehaviorSubject<number | null>(null);
  private pollSubscription?: Subscription;
  private authSubscription?: Subscription;
  private consecutiveErrors = 0;
  private destroy$ = new Subject<void>();
  // Pending rewind request info: when a client requests a rewind we keep a target
  // so the polling loop won't overwrite the local change until the server confirms it.
  private pendingRewindTargetMs: number | null = null;
  private pendingRewindRequestedAtMs: number | null = null;

  /**
   * Register a pending rewind target (iso string). While set, polling will avoid
   * applying server times until the server reports the same time (confirmation).
   */
  setPendingRewind(targetIso: string | null): void {
    if (!targetIso) {
      this.pendingRewindTargetMs = null;
      this.pendingRewindRequestedAtMs = null;
      return;
    }
    try {
      const d = new Date(targetIso);
      this.pendingRewindTargetMs = d.getTime();
      this.pendingRewindRequestedAtMs = Date.now();
      console.debug('[TimeAccelerator] pending rewind set for', d.toISOString());
    } catch (e) {
      this.pendingRewindTargetMs = null;
      this.pendingRewindRequestedAtMs = null;
    }
  }

  private clearPendingRewind(): void {
    this.pendingRewindTargetMs = null;
    this.pendingRewindRequestedAtMs = null;
  }

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService,
    private authService: AuthService,
    private simulationTimeService: SimulationTimeService
  ) {
    // Démarrer le polling automatique seulement si l'utilisateur est authentifié
    if (this.tokenStorage.getToken()) {
      this.startPolling();
    } else {
      console.log('[TimeAccelerator] User not authenticated - polling disabled');
    }

    // Écouter les événements d'authentification
    this.authService.getAuthEvents()
      .pipe(takeUntil(this.destroy$))
      .subscribe(event => {
        if (event.type === 'login') {
          console.log('[TimeAccelerator] User logged in - starting polling');
          this.startPollingIfAuthenticated();
        } else if (event.type === 'logout') {
          console.log('[TimeAccelerator] User logged out - stopping polling');
          this.stopPolling();
        }
      });
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
   * Observable pour obtenir l'information sur la compression (ex: "30 minutes")
   */
  getCompressionInfo(): Observable<string> {
    return this.compressionInfo$.asObservable();
  }

  /**
   * Observable exposant le ratio de compression (game seconds per real second)
   */
  getCompressionRatio(): Observable<number | null> {
    return this.compressionRatio$.asObservable();
  }

  /**
   * Mettre à jour le ratio de compression côté serveur
   * Attention: le endpoint doit exister côté backend (/ratio).
   */
  setTimeCompressionRatio(ratio: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/ratio`, { ratio });
  }

  /**
   * Définir le temps courant côté serveur (endpoint doit exister)
   */
  setServerGameTime(isoDate: string): Observable<any> {
    // Le backend attend un paramètre request param nommé `newTime` (ISO datetime)
    const params = { newTime: isoDate };
    // POST avec body null et params en query string
    return this.http.post(`${this.baseUrl}/set`, null, { params });
  }

  /**
   * Rewind server time to a given date (used for Time Travel)
   * Backend endpoint: POST /api/market/time/rewind?rewindToDate=...
   */
  rewindToDate(isoDate: string): Observable<any> {
    const params = { rewindToDate: isoDate };
    return this.http.post(`${this.baseUrl}/rewind`, null, { params });
  }

  /**
   * Get the current Time Travel state exposed by MarketDataController
   * Backend endpoint: GET /api/market/time/travel-state
   */
  getTimeTravelState(): Observable<any> {
    return this.http.get(`${this.baseUrl}/travel-state`);
  }

  /**
   * Check if can advance to a specific date (backend helper)
   * Backend endpoint: GET /api/market/time/can-advance?targetDate=...
   */
  canAdvanceToDate(isoDate: string): Observable<any> {
    const params = { targetDate: isoDate };
    return this.http.get(`${this.baseUrl}/can-advance`, { params });
  }

  // ===== TimeTravelController helpers (/api/time-travel) =====
  /**
   * Start a full-synchronization time travel session for a player
   * POST /api/time-travel/rewind?playerId=...&rewindToDate=...
   */
  startTimeTravelWithSync(playerId: string, isoDate: string): Observable<any> {
    const params = { playerId, rewindToDate: isoDate };
    return this.http.post(`${this.timeTravelBase}/rewind`, null, { params }).pipe(
      catchError((err) => {
        // Extract backend error message from response body
        const backendMsg = err?.error?.message || err?.error?.error || err?.error || err?.message || 'Unknown error';
        console.error('[TimeAccelerator] startTimeTravelWithSync error:', backendMsg, err);
        return throwError(() => new Error(backendMsg));
      })
    );
  }

  /**
   * Get session state from TimeTravelController
   * GET /api/time-travel/{sessionId}/state
   */
  getTimeTravelSessionState(sessionId: string): Observable<any> {
    return this.http.get(`${this.timeTravelBase}/${encodeURIComponent(sessionId)}/state`);
  }

  /**
   * Stop a time travel session
   * POST /api/time-travel/{sessionId}/stop
   */
  stopTimeTravelSession(sessionId: string): Observable<any> {
    return this.http.post(`${this.timeTravelBase}/${encodeURIComponent(sessionId)}/stop`, null);
  }

  /**
   * Get all sessions for a player
   * GET /api/time-travel/player/{playerId}/sessions
   */
  getPlayerTimeTravelSessions(playerId: string): Observable<any> {
    return this.http.get(`${this.timeTravelBase}/player/${encodeURIComponent(playerId)}/sessions`);
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
          // parse backend time (status.currentGameTime peut être Date ou String)
          const serverTime = status.currentGameTime ? new Date(status.currentGameTime as any) : null;
          // Get simulation lower bound to avoid accepting server times before simulation start
          let effectiveTime: Date | null = null;
          try {
            const bounds = this.simulationTimeService.getSimulationBounds();
            const lowerBound = bounds.start.getTime();
              if (serverTime && serverTime.getTime() >= lowerBound) {
              // server time is within simulation window — decide whether to sync local
              effectiveTime = serverTime;
              const local = this.simulationTimeService.getCurrentSimDate();
              // If a pending rewind is active, avoid overwriting the local set date
              // until the server confirms the same target time.
              if (this.pendingRewindTargetMs) {
                const diff = Math.abs(serverTime.getTime() - this.pendingRewindTargetMs);
                // If server confirms the target (within 2s), clear pending and apply
                if (diff <= 2000) {
                  this.clearPendingRewind();
                  if (!local || local.getTime() !== serverTime.getTime()) {
                    this.simulationTimeService.setCurrentSimDate(new Date(serverTime));
                  }
                } else {
                  // If the pending request is too old (10s), clear it and accept server time
                  const now = Date.now();
                  if (this.pendingRewindRequestedAtMs && now - this.pendingRewindRequestedAtMs > 10000) {
                    console.warn('[TimeAccelerator] pendingRewind timed out, accepting server time');
                    this.clearPendingRewind();
                    if (!local || local.getTime() !== serverTime.getTime()) {
                      this.simulationTimeService.setCurrentSimDate(new Date(serverTime));
                    }
                  } else {
                    // Otherwise, skip applying server time for now to avoid flicker
                    // and keep local simulated date until server confirmation.
                  }
                }
              } else {
                if (!local || local.getTime() !== serverTime.getTime()) {
                  this.simulationTimeService.setCurrentSimDate(new Date(serverTime));
                }
              }
            } else {
              // server time is before simulation start or missing — keep local simulation time
              effectiveTime = this.simulationTimeService.getCurrentSimDate();
            }
          } catch (e) {
            // Fallback: if anything fails, prefer server time if present, else null
            console.warn('[TimeAccelerator] Error determining simulation bounds for sync:', e);
            effectiveTime = serverTime || null;
          }
          this.currentGameTime$.next(effectiveTime);
          this.isActive$.next(!!status.active);
          this.compressionInfo$.next(status.compressionInfo || '');
        // Si le backend fournit un ratio numérique, l'utiliser. Sinon essayer d'extraire
        // le nombre de minutes depuis compressionInfo et convertir en ratio.
        let ratio: number | null = null;
        // backend peut renvoyer `currentRatio` dans la réponse
        // @ts-ignore
        if (status.currentRatio && typeof status.currentRatio === 'number') {
          ratio = status.currentRatio;
        } else if (status.compressionInfo) {
          // essayer d'extraire "XX minutes" depuis compressionInfo
          const m = (status.compressionInfo || '').match(/(\d+)\s*minutes?/i);
          if (m && m[1]) {
            const minutes = parseInt(m[1], 10);
            if (!isNaN(minutes) && minutes > 0) {
              const totalGameSeconds = 15 * 30 * 24 * 60 * 60; // same convention que front
              ratio = Math.round(totalGameSeconds / (minutes * 60));
            }
          }
        }
        this.compressionRatio$.next(ratio);
        // If backend provides a ratio, synchronize local SimulationTimeService
        try {
          if (ratio && typeof ratio === 'number' && this.simulationTimeService) {
            // ratio = game seconds per real second
            const minutesPerSecond = ratio / 60; // convert seconds->minutes
            this.simulationTimeService.setSimulationSpeed(minutesPerSecond);
            if (status.active) {
              // ensure local simulation runs to reflect accelerated time visually
              this.simulationTimeService.startSimulation();
            } else {
              this.simulationTimeService.stopSimulation();
            }
          }
        } catch (e) {
          console.warn('[TimeAccelerator] Failed to sync simulation speed:', e);
        }
        this.consecutiveErrors = 0; // Réinitialiser le compteur d'erreurs
      },
      error: (err) => {
        // Si le backend n'est pas disponible, on garde la dernière valeur
        // For HTTP 500 errors, fallback immediately to local simulation to avoid continuous 500s
        if (err && err.status === 500) {
          console.warn('[TimeAccelerator] Backend returned 500 - falling back to local simulation');
          try {
            this.simulationTimeService.startSimulation();
          } catch (e) {}
          // stop polling to prevent repeated 500 requests
          this.stopPolling();
          return;
        }

        // Avoid spamming console for expected errors like 401
        if (err.status !== 401) {
          console.warn('[TimeAccelerator] Backend not available:', err.message);
        }

        // Stop polling after several consecutive errors to avoid the loop
        this.consecutiveErrors = (this.consecutiveErrors || 0) + 1;
        if (this.consecutiveErrors > 5) {
          if (err.status === 401) {
            console.warn('[TimeAccelerator] Stopping polling - user not authenticated');
          } else {
            console.warn('[TimeAccelerator] Stopping polling due to repeated backend errors');
          }
          this.stopPolling();
        }
      }
    });
  }

  /**
   * Arrêter le polling (quand le composant est détruit)
   */
  stopPolling(): void {
    this.pollSubscription?.unsubscribe();
  }

  /**
   * Redémarrer le polling manuellement
   */
  restartPolling(): void {
    this.consecutiveErrors = 0;
    this.stopPolling();
    this.startPolling();
  }

  /**
   * Démarrer le polling si l'utilisateur est authentifié
   */
  startPollingIfAuthenticated(): void {
    if (this.tokenStorage.getToken()) {
      if (!this.pollSubscription || this.pollSubscription.closed) {
        this.consecutiveErrors = 0;
        this.startPolling();
        console.log('[TimeAccelerator] User authenticated - polling started');
      }
    } else {
      console.log('[TimeAccelerator] User not authenticated - cannot start polling');
    }
  }

  /**
   * Nettoyer les subscriptions quand le service est détruit
   */
  ngOnDestroy(): void {
    this.stopPolling();
    this.destroy$.next();
    this.destroy$.complete();
  }
}
