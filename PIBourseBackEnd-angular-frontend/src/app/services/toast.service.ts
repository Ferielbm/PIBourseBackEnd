import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Toast {
  message: string;
  type: 'success' | 'warning' | 'error' | 'info';
  duration?: number;
  id?: string;
  clickable?: boolean;
  onClickData?: any;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastSubject = new Subject<Toast>();
  public toast$ = this.toastSubject.asObservable();

  success(message: string, duration?: number, clickable?: boolean, onClickData?: any): void {
    this.toastSubject.next({ message, type: 'success', duration, clickable, onClickData });
  }

  warning(message: string, duration?: number, clickable?: boolean, onClickData?: any): void {
    this.toastSubject.next({ message, type: 'warning', duration, clickable, onClickData });
  }

  error(message: string, duration?: number, clickable?: boolean, onClickData?: any): void {
    this.toastSubject.next({ message, type: 'error', duration, clickable, onClickData });
  }

  info(message: string, duration?: number, clickable?: boolean, onClickData?: any): void {
    this.toastSubject.next({ message, type: 'info', duration, clickable, onClickData });
  }
}
