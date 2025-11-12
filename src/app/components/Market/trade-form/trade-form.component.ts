import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-trade-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <form class="space-y-3 text-sm" (ngSubmit)="submit()">
      <div>
        <label class="block text-gray-600 dark:text-gray-400 mb-1">Symbole</label>
        <input class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" [(ngModel)]="model.symbol" name="symbol" placeholder="AAPL" />
      </div>
      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block text-gray-600 dark:text-gray-400 mb-1">Quantité</label>
          <input type="number" class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" [(ngModel)]="model.quantity" name="quantity" min="1" placeholder="10" />
        </div>
        <div>
          <label class="block text-gray-600 dark:text-gray-400 mb-1">Prix (limite)</label>
          <input type="number" class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" [(ngModel)]="model.price" name="price" step="0.01" placeholder="205.10" />
        </div>
      </div>
      <div class="flex gap-2">
        <button type="button" class="flex-1 px-3 py-2 rounded-md bg-emerald-600 text-white hover:opacity-90" (click)="buy()">Acheter</button>
        <button type="button" class="flex-1 px-3 py-2 rounded-md bg-red-600 text-white hover:opacity-90" (click)="sell()">Vendre</button>
      </div>
    </form>
  `,
})
export class TradeFormComponent {
  @Input() symbol = 'AAPL';
  @Output() placeOrder = new EventEmitter<{ side: 'BUY'|'SELL'; symbol: string; quantity: number; price: number }>();

  model = {
    symbol: this.symbol,
    quantity: 1,
    price: 0,
  };

  ngOnChanges() {
    this.model.symbol = this.symbol;
  }

  buy() { this.emit('BUY'); }
  sell() { this.emit('SELL'); }
  submit() {}

  private emit(side: 'BUY'|'SELL') {
    const { symbol, quantity, price } = this.model;
    this.placeOrder.emit({ side, symbol, quantity: Number(quantity), price: Number(price) });
  }
}
