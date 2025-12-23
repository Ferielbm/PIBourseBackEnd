import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_BASE_URL } from '../../config/api.config';
import { LoginRequest } from '../../models/player/login-request.model';
import { JwtResponse } from '../../models/player/jwt-response.model';
import { TokenStorageService } from './token-storage.service';
import { Observable, tap, Subject, BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  
  private isAuthenticated$ = new BehaviorSubject<boolean>(false);
  private authEvents$ = new Subject<{ type: 'login' | 'logout' }>();

  login(payload: LoginRequest): Observable<JwtResponse> {
    return this.http
      .post<JwtResponse>(`${API_BASE_URL}/api/auth/login`, payload)
      .pipe(
        tap((response) => {
          this.tokenStorage.setToken(response.token);
          this.tokenStorage.setUser({
            id: response.id,
            username: response.username,
            email: response.email,
            role: response.role,
          });
          this.isAuthenticated$.next(true);
          this.authEvents$.next({ type: 'login' });
        })
      );
  }

  logout(): void {
    this.tokenStorage.clearAll();
    this.isAuthenticated$.next(false);
    this.authEvents$.next({ type: 'logout' });
  }

  getToken(): string | null {
    return this.tokenStorage.getToken();
  }

  /**
   * Observable pour savoir si l'utilisateur est authentifié
   */
  getIsAuthenticated(): Observable<boolean> {
    return this.isAuthenticated$.asObservable();
  }

  /**
   * Observable pour écouter les événements d'authentification
   */
  getAuthEvents(): Observable<{ type: 'login' | 'logout' }> {
    return this.authEvents$.asObservable();
  }

  /**
   * Vérifier si l'utilisateur est actuellement authentifié
   */
  isAuthenticated(): boolean {
    return !!this.tokenStorage.getToken();
  }
}
