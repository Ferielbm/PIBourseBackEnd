import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="grid lg:grid-cols-3 gap-4">
      <div class="lg:col-span-2 space-y-4">
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold mb-3">Positions</div>
          <div class="overflow-auto rounded-xl">
            <table class="min-w-full text-sm">
              <thead class="bg-gray-50 dark:bg-gray-800 text-gray-600 dark:text-gray-300">
                <tr>
                  <th class="text-left px-4 py-3">Symbole</th>
                  <th class="text-right px-4 py-3">Quantité</th>
                  <th class="text-right px-4 py-3">Prix moyen</th>
                  <th class="text-right px-4 py-3">Valeur</th>
                  <th class="text-right px-4 py-3">PNL</th>
                </tr>
              </thead>
              <tbody>
                <tr class="border-t border-gray-100 dark:border-gray-800">
                  <td class="px-4 py-3 font-medium">AAPL</td>
                  <td class="px-4 py-3 text-right">25</td>
                  <td class="px-4 py-3 text-right">€ 198.50</td>
                  <td class="px-4 py-3 text-right">€ 5,127.50</td>
                  <td class="px-4 py-3 text-right text-emerald-600">+€ 162.50</td>
                </tr>
                <tr class="border-t border-gray-100 dark:border-gray-800">
                  <td class="px-4 py-3 font-medium">MSFT</td>
                  <td class="px-4 py-3 text-right">10</td>
                  <td class="px-4 py-3 text-right">€ 380.00</td>
                  <td class="px-4 py-3 text-right">€ 3,924.50</td>
                  <td class="px-4 py-3 text-right text-emerald-600">+€ 45.00</td>
                </tr>
                <tr class="border-t border-gray-100 dark:border-gray-800">
                  <td class="px-4 py-3 font-medium">TSLA</td>
                  <td class="px-4 py-3 text-right">12</td>
                  <td class="px-4 py-3 text-right">€ 210.00</td>
                  <td class="px-4 py-3 text-right">€ 2,547.60</td>
                  <td class="px-4 py-3 text-right text-red-600">-€ 32.40</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="space-y-4">
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold">Performance</div>
          <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
            <div>
              <div class="text-gray-500 dark:text-gray-400">Valeur totale</div>
              <div class="font-semibold">€ 11,599.60</div>
            </div>
            <div>
              <div class="text-gray-500 dark:text-gray-400">Rentabilité</div>
              <div class="font-semibold text-emerald-600">+8.7%</div>
            </div>
            <div>
              <div class="text-gray-500 dark:text-gray-400">PNL réalisé</div>
              <div class="font-semibold">€ 320.00</div>
            </div>
            <div>
              <div class="text-gray-500 dark:text-gray-400">PNL non réalisé</div>
              <div class="font-semibold">€ 175.10</div>
            </div>
          </div>
        </div>
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
          <div class="font-semibold mb-3">Allocation</div>
          <div class="space-y-2 text-sm">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-indigo-500"></span>AAPL</div>
              <div>44%</div>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-emerald-500"></span>MSFT</div>
              <div>34%</div>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-fuchsia-500"></span>TSLA</div>
              <div>22%</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class PortfolioComponent {

}
