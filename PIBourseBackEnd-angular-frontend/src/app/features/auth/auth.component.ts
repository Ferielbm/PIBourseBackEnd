import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest, RegisterRequest } from '../../models/auth.model';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 p-4">
      <div class="w-full max-w-md">
        <!-- Logo et titre -->
        <div class="text-center mb-8">
          <h1 class="text-5xl font-bold text-white mb-2">📈 PiBourse</h1>
          <p class="text-white/80 text-lg">Plateforme de trading boursier</p>
        </div>

        <!-- Card principale -->
        <div class="bg-white rounded-2xl shadow-2xl p-8">
          <!-- Tabs -->
          <div class="flex gap-2 mb-6 bg-gray-100 rounded-lg p-1">
            <button
              (click)="activeTab = 'login'"
              [class.bg-white]="activeTab === 'login'"
              [class.shadow]="activeTab === 'login'"
              class="flex-1 py-2 px-4 rounded-md font-medium transition"
              [class.text-indigo-600]="activeTab === 'login'"
              [class.text-gray-600]="activeTab !== 'login'"
            >
              Connexion
            </button>
            <button
              (click)="activeTab = 'register'"
              [class.bg-white]="activeTab === 'register'"
              [class.shadow]="activeTab === 'register'"
              class="flex-1 py-2 px-4 rounded-md font-medium transition"
              [class.text-indigo-600]="activeTab === 'register'"
              [class.text-gray-600]="activeTab !== 'register'"
            >
              Inscription
            </button>
          </div>

          <!-- Message d'erreur -->
          <div *ngIf="errorMessage" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-red-600 text-sm">{{ errorMessage }}</p>
          </div>

          <!-- Message de succès -->
          <div *ngIf="successMessage" class="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg">
            <p class="text-green-600 text-sm">{{ successMessage }}</p>
          </div>

          <!-- Formulaire de connexion -->
          <form *ngIf="activeTab === 'login'" (ngSubmit)="onLogin()" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                [(ngModel)]="loginData.email"
                name="email"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="votre@email.com"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe</label>
              <input
                type="password"
                [(ngModel)]="loginData.password"
                name="password"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              [disabled]="isLoading"
              class="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
            >
              {{ isLoading ? 'Connexion...' : 'Se connecter' }}
            </button>
          </form>

          <!-- Formulaire d'inscription -->
          <form *ngIf="activeTab === 'register'" (ngSubmit)="onRegister()" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nom d'utilisateur</label>
              <input
                type="text"
                [(ngModel)]="registerData.username"
                name="username"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="John Doe"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                [(ngModel)]="registerData.email"
                name="email"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="votre@email.com"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe</label>
              <input
                type="password"
                [(ngModel)]="registerData.password"
                name="password"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="••••••••"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Rôle</label>
              <select
                [(ngModel)]="registerData.role"
                name="role"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              >
                <option value="ROLE_PLAYER">👤 Joueur</option>
                <option value="ROLE_MENEUR_JEU">🎮 Meneur de Jeu</option>
              </select>
              <p class="text-xs text-gray-500 mt-1">
                Choisissez "Meneur de Jeu" pour accéder au dashboard de gestion du marché
              </p>
            </div>

            <button
              type="submit"
              [disabled]="isLoading"
              class="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
            >
              {{ isLoading ? 'Inscription...' : "S'inscrire" }}
            </button>
          </form>

          <!-- Accès rapide de test -->
          <div class="mt-6 pt-6 border-t border-gray-200">
            <p class="text-sm text-gray-600 text-center mb-3">Accès rapide (test)</p>
            <div class="flex gap-2">
              <button
                (click)="quickLogin('player')"
                class="flex-1 px-3 py-2 bg-blue-50 text-blue-600 rounded-lg text-sm hover:bg-blue-100 transition"
              >
                👤 Joueur
              </button>
              <button
                (click)="quickLogin('gm')"
                class="flex-1 px-3 py-2 bg-purple-50 text-purple-600 rounded-lg text-sm hover:bg-purple-100 transition"
              >
                🎮 Meneur
              </button>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <p class="text-center text-white/60 text-sm mt-6">
          © 2024 PiBourse - Plateforme éducative de trading
        </p>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100vh;
    }
  `]
})
export class AuthComponent {
  activeTab: 'login' | 'register' = 'login';
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  loginData: LoginRequest = {
    email: '',
    password: ''
  };

  registerData: RegisterRequest = {
    username: '',
    email: '',
    password: '',
    role: 'ROLE_PLAYER'
  };

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Connexion réussie! Redirection...';
        
        setTimeout(() => {
          // Rediriger selon le rôle
          if (this.authService.isGameMaster()) {
            this.router.navigate(['/game-master']);
          } else {
            this.router.navigate(['/market']);
          }
        }, 1000);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Email ou mot de passe incorrect';
      }
    });
  }

  onRegister(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Inscription réussie! Redirection...';
        
        setTimeout(() => {
          // Rediriger selon le rôle
          if (this.authService.isGameMaster()) {
            this.router.navigate(['/game-master']);
          } else {
            this.router.navigate(['/market']);
          }
        }, 1000);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || "Erreur lors de l'inscription";
      }
    });
  }

  /**
   * Connexion rapide pour les tests
   */
  quickLogin(type: 'player' | 'gm'): void {
    if (type === 'player') {
      this.loginData = { email: 'player@test.com', password: 'player123' };
    } else {
      this.loginData = { email: 'gm@test.com', password: 'gm123' };
    }
    this.activeTab = 'login';
  }
}
