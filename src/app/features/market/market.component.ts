import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="grid xl:grid-cols-3 gap-4">
      <!-- Chart + Ticker -->
      <div class="xl:col-span-2 space-y-4 min-w-0">
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="flex items-center justify-between mb-3">
            <div class="font-semibold">AAPL / EUR</div>
            <div class="text-sm text-gray-500 dark:text-gray-400">Marché simulé</div>
          </div>
          <!-- Chart placeholder -->
          <div class="h-80 rounded-xl bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-850 relative overflow-hidden">
            <div class="absolute inset-0 opacity-30 [background-image:repeating-linear-gradient(90deg,transparent,transparent_30px,rgba(0,0,0,.06)_31px)]"></div>
            <div class="absolute inset-0 opacity-30 [background-image:repeating-linear-gradient(0deg,transparent,transparent_30px,rgba(0,0,0,.06)_31px)]"></div>
            <div class="absolute inset-0 flex items-center justify-center text-gray-400">Graphique prix (placeholder)</div>
          </div>
        </div>
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold mb-3">Tickers récents</div>
          <div class="grid md:grid-cols-3 gap-3 text-sm">
            <div class="p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
              <div class="text-gray-500">AAPL</div>
              <div class="font-semibold">€ 205.12 <span class="text-emerald-600">+1.2%</span></div>
            </div>
            <div class="p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
              <div class="text-gray-500">MSFT</div>
              <div class="font-semibold">€ 392.45 <span class="text-emerald-600">+0.8%</span></div>
            </div>
            <div class="p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
              <div class="text-gray-500">TSLA</div>
              <div class="font-semibold">€ 212.30 <span class="text-red-600">-0.6%</span></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Trade panel -->
      <div class="space-y-4">
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold mb-4">Passer un ordre</div>
          <div class="space-y-3 text-sm">
            <div>
              <label class="block text-gray-600 dark:text-gray-400 mb-1">Symbole</label>
              <input class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" placeholder="AAPL" />
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-gray-600 dark:text-gray-400 mb-1">Quantité</label>
                <input type="number" class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" placeholder="10" />
              </div>
              <div>
                <label class="block text-gray-600 dark:text-gray-400 mb-1">Prix (limite)</label>
                <input type="number" class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" placeholder="205.10" />
              </div>
            </div>
            <div class="flex gap-2">
              <button class="flex-1 px-3 py-2 rounded-md bg-emerald-600 text-white hover:opacity-90">Acheter</button>
              <button class="flex-1 px-3 py-2 rounded-md bg-red-600 text-white hover:opacity-90">Vendre</button>
            </div>
          </div>
        </div>
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold mb-2">Résumé</div>
          <div class="text-sm text-gray-600 dark:text-gray-400">Solde dispo: <span class="text-gray-900 dark:text-gray-200 font-semibold">€ 3,330.32</span></div>
          <div class="text-sm text-gray-600 dark:text-gray-400">Exposition: <span class="text-gray-900 dark:text-gray-200 font-semibold">€ 9,120.00</span></div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class MarketComponent {

}
