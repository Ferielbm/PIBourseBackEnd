import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { trigger, transition, style, animate } from '@angular/animations';
import { ToastService, Toast } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-4 right-4 space-y-2 pointer-events-none" style="z-index: 999999;">
      <div
        *ngFor="let toast of toasts"
        @toastAnimation
        (click)="onToastClick(toast)"
        class="flex items-start gap-3 px-6 py-4 rounded-lg shadow-lg pointer-events-auto max-w-md min-w-[280px]"
        [ngClass]="getToastClasses(toast.type)"
        [class.cursor-pointer]="toast.clickable"
        [class.hover:scale-105]="toast.clickable"
        [class.transition-transform]="toast.clickable"
      >
        <span class="text-2xl leading-none">{{ getToastIcon(toast.type) }}</span>
        <div class="flex-1">
          <p class="text-base font-medium whitespace-nowrap">{{ toast.message }}</p>
          <p *ngIf="toast.clickable" class="text-xs mt-1 opacity-70">
            {{ getClickHint(toast) }}
          </p>
        </div>
        <button
          (click)="removeToast(toast.id || ''); $event.stopPropagation()"
          class="text-current opacity-50 hover:opacity-100 transition flex-shrink-0"
        >
          ✕
        </button>
      </div>
    </div>
  `,
  animations: [
    trigger('toastAnimation', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateX(100px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ opacity: 0, transform: 'translateX(100px)' }))
      ])
    ])
  ]
})
export class ToastContainerComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];

  constructor(
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.toastService.toast$.subscribe((toast: Toast) => {
      this.addToast(toast);
    });
  }

  addToast(toast: Toast): void {
    const id = Date.now().toString();
    this.toasts.push({ ...toast, id });

    if (toast.duration !== 0) {
      const duration = toast.duration || 5000;
      setTimeout(() => this.removeToast(id), duration);
    }
  }

  removeToast(id: string): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  onToastClick(toast: Toast): void {
    console.log('🖱️ Toast cliqué:', toast);
    console.log('   - clickable:', toast.clickable);
    console.log('   - onClickData:', toast.onClickData);
    
    if (toast.clickable && toast.onClickData) {
      const route = toast.onClickData.route || '/market';
      const symbol = toast.onClickData.symbol;
      
      console.log('🔗 Navigation vers:', route, 'avec symbole:', symbol);
      
      // Construire la navigation avec ou sans queryParams
      const navigationExtras = symbol ? { queryParams: { symbol } } : {};
      
      this.router.navigate([route], navigationExtras).then(success => {
        console.log('✅ Navigation réussie:', success);
        if (toast.id) {
          this.removeToast(toast.id);
        }
      }).catch(err => {
        console.error('❌ Erreur navigation:', err);
      });
    } else {
      console.warn('⚠️ Toast non cliquable ou pas de données');
    }
  }

  getToastIcon(type: string): string {
    switch (type) {
      case 'success': return '✅';
      case 'warning': return '⚠️';
      case 'error': return '❌';
      case 'info': return 'ℹ️';
      default: return '•';
    }
  }

  getClickHint(toast: Toast): string {
    if (toast.onClickData?.route === '/decision-tickets') {
      return '👆 Cliquez pour voir les tickets';
    }
    if (toast.onClickData?.route === '/orders' || toast.onClickData?.symbol) {
      return '👆 Cliquez pour voir les ordres';
    }
    return '👆 Cliquez pour plus d\'infos';
  }

  getToastClasses(type: string): string {
    switch (type) {
      case 'success':
        return 'bg-emerald-100 text-emerald-900 dark:bg-emerald-900/30 dark:text-emerald-200';
      case 'warning':
        return 'bg-amber-100 text-amber-900 dark:bg-amber-900/30 dark:text-amber-200';
      case 'error':
        return 'bg-red-100 text-red-900 dark:bg-red-900/30 dark:text-red-200';
      case 'info':
      default:
        return 'bg-blue-100 text-blue-900 dark:bg-blue-900/30 dark:text-blue-200';
    }
  }

  ngOnDestroy(): void {
    this.toasts = [];
  }
}
