import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../environment/environment';
import { User, LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private readonly STORAGE_KEY = 'pibourse_user';

  constructor(private http: HttpClient) {
    // Charger l'utilisateur depuis le localStorage au démarrage
    this.loadUserFromStorage();
  }

  /**
   * Inscription
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, request)
      .pipe(
        tap(response => {
          if (response.playerId) {
            this.setCurrentUser({
              playerId: response.playerId,
              username: response.username,
              email: response.email,
              role: response.role
            });
          }
        })
      );
  }

  /**
   * Connexion
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(
        tap(response => {
          if (response.playerId) {
            this.setCurrentUser({
              playerId: response.playerId,
              username: response.username,
              email: response.email,
              role: response.role
            });
          }
        })
      );
  }

  /**
   * Déconnexion
   */
  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Vérifier si l'utilisateur est connecté
   */
  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  /**
   * Vérifier si l'utilisateur est un meneur de jeu
   */
  isGameMaster(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ROLE_MENEUR_JEU' || user?.role === 'ROLE_ADMIN';
  }

  /**
   * Obtenir l'utilisateur actuel
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Obtenir l'ID du joueur actuel
   */
  getCurrentPlayerId(): number | null {
    return this.currentUserSubject.value?.playerId || null;
  }

  /**
   * Définir l'utilisateur actuel
   */
  private setCurrentUser(user: User): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  /**
   * Charger l'utilisateur depuis le localStorage
   */
  private loadUserFromStorage(): void {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (stored) {
      try {
        const user = JSON.parse(stored);
        this.currentUserSubject.next(user);
      } catch (e) {
        console.error('Erreur chargement utilisateur:', e);
      }
    }
  }
}
