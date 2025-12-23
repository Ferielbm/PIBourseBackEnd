// src/app/features/orders/services/orderbook.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BookSnapshot } from '../../../models/Order/order.models';
import { OrderService } from './order.service';

@Injectable({
  providedIn: 'root'
})
export class OrderbookService {

  constructor(private orderService: OrderService) {}

  getBook(playerId: number, symbol: string): Observable<BookSnapshot> {
    return this.orderService.getOrderBook(playerId, symbol);
  }
}
