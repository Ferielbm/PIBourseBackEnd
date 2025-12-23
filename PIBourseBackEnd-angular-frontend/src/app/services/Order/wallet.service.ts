// src/app/features/orders/services/wallet.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError, switchMap, of } from 'rxjs';
import { Wallet } from '../../models/Order/wallet.models';
import { WalletReservation } from '../../models/Order/wallet-reservation.models';
import { environment } from '../../environment/environment';

@Injectable({
  providedIn: 'root'
})
export class WalletService {

  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET wallet with fallback if route differs on backend
  getWallet(playerId: number): Observable<Wallet> {
    const primary = `${this.baseUrl}/players/${playerId}/wallet`; // ex: http://host/api/players/4/wallet
    const fallbackNoApi = primary.replace('/api/', '/'); // http://host/players/4/wallet
    const altPattern = `${this.baseUrl}/wallet/${playerId}`; // ex: /api/wallet/4 (possible alternate design)

    return this.http.get<Wallet>(primary).pipe(
      catchError(err => {
        const msg = (err?.error && JSON.stringify(err.error)) || err.message || '';
        // If backend returns NoResourceFound / 404, try fallbacks
        const isNotFound = err.status === 404 || /NoResourceFound/i.test(msg) || /No static resource/i.test(msg);
        if (!isNotFound) {
          return throwError(() => err);
        }
        // Try /players without /api
        return this.http.get<Wallet>(fallbackNoApi).pipe(
          catchError(err2 => {
            const msg2 = (err2?.error && JSON.stringify(err2.error)) || err2.message || '';
            const isNotFound2 = err2.status === 404 || /NoResourceFound/i.test(msg2) || /No static resource/i.test(msg2);
            if (!isNotFound2) {
              return throwError(() => err2);
            }
            // Try /api/wallet/{playerId}
            return this.http.get<Wallet>(altPattern).pipe(
              catchError(err3 => {
                // Final failure: craft a clearer error object
                const friendly = {
                  error: 'WALLET_ENDPOINT_NOT_FOUND',
                  tried: [primary, fallbackNoApi, altPattern],
                  message: 'Aucun endpoint wallet trouvé sur le backend. Vérifiez le mapping @GetMapping ou ajustez environment.apiUrl.'
                };
                return throwError(() => friendly);
              })
            );
          })
        );
      })
    );
  }

  // GET /players/{playerId}/wallet/reservations
  getWalletReservations(playerId: number): Observable<WalletReservation[]> {
    return this.http.get<WalletReservation[]>(
      `${this.baseUrl}/players/${playerId}/wallet/reservations`
    );
  }
}
