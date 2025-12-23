import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-4">
      <div class="flex flex-wrap items-center gap-2">
        <select class="px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-sm">
          <option>Toutes</option>
          <option>Achat</option>
          <option>Vente</option>
        </select>
        <select class="px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-sm">
          <option>Statut: Tous</option>
          <option>Exécuté</option>
          <option>Partiel</option>
          <option>Annulé</option>
        </select>
      </div>
      <div class="overflow-auto rounded-xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900">
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 dark:bg-gray-800 text-gray-600 dark:text-gray-300">
            <tr>
              <th class="text-left px-4 py-3">Date</th>
              <th class="text-left px-4 py-3">Type</th>
              <th class="text-left px-4 py-3">Symbole</th>
              <th class="text-right px-4 py-3">Quantité</th>
              <th class="text-right px-4 py-3">Prix</th>
              <th class="text-right px-4 py-3">Montant</th>
              <th class="text-right px-4 py-3">Statut</th>
            </tr>
          </thead>
          <tbody>
            <tr class="border-t border-gray-100 dark:border-gray-800">
              <td class="px-4 py-3">04/11/2025 18:22</td>
              <td class="px-4 py-3"><span class="px-2 py-0.5 rounded bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300">Achat</span></td>
              <td class="px-4 py-3">AAPL</td>
              <td class="px-4 py-3 text-right">10</td>
              <td class="px-4 py-3 text-right">€ 205.10</td>
              <td class="px-4 py-3 text-right">€ 2,051.00</td>
              <td class="px-4 py-3 text-right"><span class="px-2 py-0.5 rounded bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300">Exécuté</span></td>
            </tr>
            <tr class="border-t border-gray-100 dark:border-gray-800">
              <td class="px-4 py-3">04/11/2025 18:35</td>
              <td class="px-4 py-3"><span class="px-2 py-0.5 rounded bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300">Vente</span></td>
              <td class="px-4 py-3">TSLA</td>
              <td class="px-4 py-3 text-right">5</td>
              <td class="px-4 py-3 text-right">€ 212.00</td>
              <td class="px-4 py-3 text-right">€ 1,060.00</td>
              <td class="px-4 py-3 text-right"><span class="px-2 py-0.5 rounded bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300">Partiel</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: ``
})
export class OrdersComponent {

}
