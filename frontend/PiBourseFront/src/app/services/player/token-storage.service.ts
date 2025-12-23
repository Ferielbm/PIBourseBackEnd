import { Injectable } from '@angular/core';

const TOKEN_KEY = 'player_token';
const USER_KEY = 'player_info';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  setUser(payload: unknown): void {
    localStorage.setItem(USER_KEY, JSON.stringify(payload));
  }

  getUser<T = unknown>(): T | null {
    const data = localStorage.getItem(USER_KEY);
    return data ? (JSON.parse(data) as T) : null;
  }

  clearUser(): void {
    localStorage.removeItem(USER_KEY);
  }

  clearAll(): void {
    this.clearToken();
    this.clearUser();
  }
}
