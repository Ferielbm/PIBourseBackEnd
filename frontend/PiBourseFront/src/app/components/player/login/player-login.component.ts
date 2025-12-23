import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../services/player/auth.service';
import { LoginRequest } from '../../../models/player/login-request.model';

@Component({
  selector: 'app-player-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-slate-950 text-slate-100">
      <div class="w-full max-w-md space-y-8 p-10 rounded-3xl bg-slate-900/80 border border-slate-800 shadow-2xl">
        <div class="space-y-2 text-center">
          <h1 class="text-2xl font-semibold">Connexion PiBourse</h1>
          <p class="text-sm text-slate-400">Connectez-vous pour accéder au marché.</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="submit()" class="space-y-6">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-slate-300">Email ou nom d'utilisateur</label>
            <input
              formControlName="identifier"
              type="text"
              class="w-full rounded-lg border border-slate-700 bg-slate-800 px-4 py-2 text-slate-100 focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/30"
              placeholder="john.doe@example.com"
            />
            <p class="text-xs text-rose-400" *ngIf="identifierInvalid()">
              Renseignez votre email ou nom d'utilisateur.
            </p>
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-slate-300">Mot de passe</label>
            <input
              formControlName="password"
              type="password"
              autocomplete="current-password"
              class="w-full rounded-lg border border-slate-700 bg-slate-800 px-4 py-2 text-slate-100 focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/30"
              placeholder="••••••••"
            />
            <p class="text-xs text-rose-400" *ngIf="passwordInvalid()">
              Le mot de passe est requis (6 caractères minimum).
            </p>
          </div>

          <button
            type="submit"
            class="w-full rounded-lg bg-cyan-500 px-4 py-2 font-medium text-white shadow-lg shadow-cyan-500/40 transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:bg-slate-700 disabled:shadow-none"
            [disabled]="loginForm.invalid || loading()"
          >
            <span *ngIf="!loading(); else loadingTpl">Se connecter</span>
          </button>
        </form>

        <ng-template #loadingTpl>
          <span class="flex items-center justify-center gap-2">
            <span class="h-3 w-3 animate-ping rounded-full bg-white"></span>
            Connexion en cours...
          </span>
        </ng-template>

        <div *ngIf="errorMessage()" class="rounded-lg border border-rose-500/50 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
          {{ errorMessage() }}
        </div>

        <div *ngIf="successMessage()" class="rounded-lg border border-emerald-500/40 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
          {{ successMessage() }}
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class PlayerLoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loginForm = this.fb.nonNullable.group({
    identifier: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  private readonly loadingSignal = signal(false);
  loading = computed(() => this.loadingSignal());

  private readonly errorSignal = signal<string | null>(null);
  errorMessage = computed(() => this.errorSignal());

  private readonly successSignal = signal<string | null>(null);
  successMessage = computed(() => this.successSignal());

  constructor() {
    effect(() => {
      if (!this.loading()) {
        this.loginForm.enable({ emitEvent: false });
      } else {
        this.loginForm.disable({ emitEvent: false });
      }
    });
  }

  identifierInvalid(): boolean {
    const control = this.loginForm.controls.identifier;
    return control.invalid && (control.dirty || control.touched);
  }

  passwordInvalid(): boolean {
    const control = this.loginForm.controls.password;
    return control.invalid && (control.dirty || control.touched);
  }

  submit(): void {
    if (this.loginForm.invalid || this.loading()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const identifier = this.loginForm.controls.identifier.value.trim();
    const password = this.loginForm.controls.password.value.trim();

    const payload: LoginRequest = identifier.includes('@')
      ? { email: identifier, password }
      : { username: identifier, password };

    this.loadingSignal.set(true);
    this.errorSignal.set(null);
    this.successSignal.set(null);

    this.authService
      .login(payload)
      .pipe(finalize(() => this.loadingSignal.set(false)))
      .subscribe({
        next: () => {
          this.successSignal.set('Connexion réussie ! Redirection en cours...');
          this.router.navigate(['/market']);
        },
        error: (error) => {
          const serverMsg = error?.error?.message || error?.message;
          this.errorSignal.set(serverMsg || 'Connexion impossible. Vérifiez vos identifiants.');
        },
      });
  }
}
