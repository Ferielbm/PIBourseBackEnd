import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, NgClass],
  template: `
    <div class="h-screen w-full flex bg-white text-gray-900 dark:bg-gray-950 dark:text-gray-100">
      <!-- Sidebar -->
      <aside class="w-72 shrink-0 border-r border-gray-200 dark:border-gray-800 bg-gray-50/60 dark:bg-gray-900/60 backdrop-blur">
        <div class="h-16 flex items-center px-4 border-b border-gray-200 dark:border-gray-800">
          <div class="text-xl font-bold tracking-tight">PiBourse</div>
        </div>
        <nav class="p-3 space-y-1">
          <a routerLink="/player" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-indigo-600 group-hover:text-indigo-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14a5 5 0 1 0 0-10 5 5 0 0 0 0 10Zm-7 7a7 7 0 1 1 14 0H5Z"/></svg>
            <span class="font-medium">Player</span>
          </a>
          <a routerLink="/market" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-emerald-600 group-hover:text-emerald-500" viewBox="0 0 24 24" fill="currentColor"><path d="M3 3h2v18H3V3Zm16 0h2v18h-2V3ZM8 13l3-3 3 3 4-4v9H4v-2h4v-3Z"/></svg>
            <span class="font-medium">Salle de marché</span>
          </a>
          <a routerLink="/orders" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-amber-600 group-hover:text-amber-500" viewBox="0 0 24 24" fill="currentColor"><path d="M7 3h10a2 2 0 0 1 2 2v3H5V5a2 2 0 0 1 2-2Zm-2 8h14v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6Zm4 2v2h6v-2H9Z"/></svg>
            <span class="font-medium">Carnet d'ordre</span>
          </a>
          <a routerLink="/portfolio" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-fuchsia-600 group-hover:text-fuchsia-500" viewBox="0 0 24 24" fill="currentColor"><path d="M3 7a2 2 0 0 1 2-2h3l2-2h4l2 2h3a2 2 0 0 1 2 2v3H3V7Zm0 5h18v5a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-5Z"/></svg>
            <span class="font-medium">Portefeuille</span>
          </a>
          <a routerLink="/credit" routerLinkActive="!bg-indigo-600 !text-white" class="group flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white transition">
            <svg class="h-5 w-5 text-cyan-600 group-hover:text-cyan-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 1a4 4 0 0 1 4 4v1h2a3 3 0 0 1 3 3v3H3V9a3 3 0 0 1 3-3h2V5a4 4 0 0 1 4-4Zm-9 13h18v2a3 3 0 0 1-3 3h-4v2h-4v-2H6a3 3 0 0 1-3-3v-2Z"/></svg>
            <span class="font-medium">Crédit</span>
          </a>
        </nav>
      </aside>

      <!-- Content area -->
      <section class="flex-1 flex flex-col min-w-0">
        <!-- Topbar -->
        <header class="h-16 flex items-center justify-between px-4 border-b border-gray-200 dark:border-gray-800 bg-white/60 dark:bg-gray-950/60 backdrop-blur">
          <div class="font-semibold text-lg">Salle de marché boursière</div>
          <button (click)="toggleTheme()" class="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-gray-900 text-white dark:bg-gray-100 dark:text-gray-900 hover:opacity-90 transition">
            <svg *ngIf="!isDark" class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 18a6 6 0 1 1 0-12 6 6 0 0 1 0 12Zm0 4v-2m0-16V2m10 10h-2M6 12H4m13.657 6.657-1.414-1.414M7.757 7.757 6.343 6.343m12.728 0-1.414 1.414M7.757 16.243l-1.414 1.414" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <svg *ngIf="isDark" class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z"/></svg>
            <span>{{ isDark ? 'Light' : 'Dark' }} mode</span>
          </button>
        </header>
        <main class="flex-1 overflow-auto p-4 bg-gray-50 dark:bg-gray-950">
          <router-outlet />
        </main>
      </section>
    </div>
  `,
  styles: ``
})
export class ShellComponent {
  isDark = false;

  constructor() {
    const saved = localStorage.getItem('theme');
    this.isDark = saved ? saved === 'dark' : window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.applyTheme();
  }

  toggleTheme() {
    this.isDark = !this.isDark;
    localStorage.setItem('theme', this.isDark ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme() {
    const root = document.documentElement;
    if (this.isDark) root.classList.add('dark');
    else root.classList.remove('dark');
  }
}
