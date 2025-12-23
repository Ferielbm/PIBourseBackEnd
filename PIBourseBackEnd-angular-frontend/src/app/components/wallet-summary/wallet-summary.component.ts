import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService } from '../../services/Order/wallet.service';
import { AuthService } from '../../services/auth.service';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-wallet-summary',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Mode Compact (dans market) -->
    <div *ngIf="mode === 'compact'" class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
      <div class="font-semibold mb-3">💰 Wallet</div>
      <div *ngIf="wallet" class="space-y-2 text-sm">
        <div class="flex justify-between">
          <span class="text-gray-600 dark:text-gray-400">Solde:</span>
          <span class="font-semibold text-gray-900 dark:text-gray-200">{{ wallet.balance | number:'1.2-2' }}€</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600 dark:text-gray-400">Réservé:</span>
          <span class="font-semibold text-orange-600">-{{ wallet.reserved | number:'1.2-2' }}€</span>
        </div>
        <div class="flex justify-between pt-2 border-t border-gray-200 dark:border-gray-700">
          <span class="text-gray-600 dark:text-gray-400">Disponible:</span>
          <span class="font-semibold text-emerald-600">{{ wallet.available | number:'1.2-2' }}€</span>
        </div>
      </div>
      <div *ngIf="!wallet && !error" class="text-sm text-gray-500">Chargement...</div>
      <div *ngIf="error" class="text-sm text-red-500">{{ error }}</div>
    </div>

    <!-- Mode Button (dans header) - Badge simple avec montant disponible -->
    <div *ngIf="mode === 'button'" class="relative">
      <div *ngIf="wallet" class="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
        <span class="text-lg">💰</span>
        <div class="flex flex-col text-xs">
          <span class="text-gray-500 dark:text-gray-400">Disponible</span>
          <span class="font-bold text-emerald-600 dark:text-emerald-400">{{ wallet.available | number:'1.2-2' }}€</span>
        </div>
        <span *ngIf="wallet.available <= 100" class="ml-1 h-2 w-2 bg-red-500 rounded-full animate-pulse"></span>
      </div>
      <div *ngIf="!wallet && !error" class="px-3 py-1.5 text-xs text-gray-500">
        <span class="text-lg">💰</span> ...
      </div>
      <div *ngIf="error" class="px-3 py-1.5 text-xs text-red-500">
        ❌ Erreur
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class WalletSummaryComponent implements OnInit, OnDestroy {
  @Input() mode: 'compact' | 'button' = 'compact'; // 'compact' pour market, 'button' pour header
  
  wallet: any = null;
  error: string = '';
  playerId: number = 0;
  private refreshSubscription?: Subscription;

  constructor(
    private walletService: WalletService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log(`💰 WalletSummary: Initialisation mode=${this.mode}`);
    this.playerId = this.authService.getCurrentPlayerId() || 0;
    console.log(`💰 WalletSummary: PlayerId détecté = ${this.playerId}`);
    
    if (!this.playerId) {
      this.error = 'Aucun joueur connecté';
      console.error('❌ WalletSummary: Aucun playerId disponible');
      return;
    }

    // Charger immédiatement
    this.loadWallet();
    
    // Auto-refresh toutes les 5 secondes
    this.refreshSubscription = interval(5000).pipe(
      switchMap(() => {
        const currentPlayerId = this.authService.getCurrentPlayerId() || 0;
        // Vérifier si le joueur a changé
        if (currentPlayerId !== this.playerId) {
          console.log(`💰 WalletSummary: Changement de joueur détecté ${this.playerId} → ${currentPlayerId}`);
          this.playerId = currentPlayerId;
        }
        return this.walletService.getWallet(this.playerId);
      })
    ).subscribe({
      next: (data) => {
        this.wallet = data;
        this.error = '';
      },
      error: (err) => {
        console.error('❌ Erreur refresh wallet:', err);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  loadWallet(): void {
    if (!this.playerId) {
      this.error = 'Joueur non connecté';
      console.error('❌ Wallet: playerId manquant');
      return;
    }

    console.log(`🔄 Chargement wallet pour player ${this.playerId}, mode: ${this.mode}`);
    this.walletService.getWallet(this.playerId).subscribe({
      next: (data) => {
        this.wallet = data;
        this.error = ''; // Clear error on success
        console.log('✅ Wallet chargé:', {
          mode: this.mode,
          balance: data.balance,
          reserved: data.reserved,
          available: data.available,
          calcul: `${data.balance} - ${data.reserved} = ${data.available}`,
          fullData: data
        });
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement du wallet:', err);
        this.error = 'Erreur de chargement du wallet';
      }
    });
  }
}
