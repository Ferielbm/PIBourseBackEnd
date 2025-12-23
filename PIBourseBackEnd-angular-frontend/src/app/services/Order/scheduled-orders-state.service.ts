import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ScheduledOrdersStateService {
  private orderCreated$ = new Subject<void>();

  getOrderCreated$ = () => this.orderCreated$.asObservable();

  notifyOrderCreated(): void {
    this.orderCreated$.next();
  }
}
