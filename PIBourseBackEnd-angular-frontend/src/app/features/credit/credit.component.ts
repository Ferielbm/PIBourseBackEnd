import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-credit',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="grid lg:grid-cols-3 gap-4">
      <div class="lg:col-span-2 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-6">
        <div class="font-semibold text-lg mb-4">Emprunter de la monnaie virtuelle</div>
        <div class="space-y-4 text-sm">
          <div class="grid md:grid-cols-2 gap-4">
            <div>
              <label class="block text-gray-600 dark:text-gray-400 mb-1">Montant (EUR)</label>
              <input type="number" class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700" placeholder="1000" />
            </div>
            <div>
              <label class="block text-gray-600 dark:text-gray-400 mb-1">Durée</label>
              <select class="w-full px-3 py-2 rounded-md bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
                <option>30 jours</option>
                <option>60 jours</option>
                <option>90 jours</option>
              </select>
            </div>
          </div>
          <div class="grid md:grid-cols-3 gap-4">
            <div>
              <label class="block text-gray-600 dark:text-gray-400 mb-1">Taux d'intérêt</label>
              <div class="font-medium">2.5% / mois</div>
            </div>
            <div>
              <label class="block text-gray-600 dark:text-gray-400 mb-1">Frais</label>
              <div class="font-medium">€ 5.00</div>
            </div>
            <div>
              <label class="block text-gray-600 dark:text-gray-400 mb-1">Echéance</label>
              <div class="font-medium">05/12/2025</div>
            </div>
          </div>
          <div class="flex gap-3">
            <button class="px-4 py-2 rounded-md bg-indigo-600 text-white hover:opacity-90">Demander crédit</button>
            <button class="px-4 py-2 rounded-md bg-gray-200 dark:bg-gray-800 hover:opacity-90">Simuler</button>
          </div>
        </div>
      </div>
      <div class="space-y-4">
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-6">
          <div class="font-semibold mb-3">Résumé crédit</div>
          <div class="space-y-2 text-sm">
            <div class="flex items-center justify-between"><span>Plafond</span><span class="font-semibold">€ 5,000</span></div>
            <div class="flex items-center justify-between"><span>Utilisé</span><span class="font-semibold">€ 1,250</span></div>
            <div class="flex items-center justify-between"><span>Disponible</span><span class="font-semibold">€ 3,750</span></div>
          </div>
        </div>
        <div class="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-6">
          <div class="font-semibold mb-2">Dernières opérations</div>
          <ul class="text-sm space-y-2">
            <li class="flex items-center justify-between"><span>+ € 1,000 (30j)</span><span class="text-gray-500">04/10/2025</span></li>
            <li class="flex items-center justify-between"><span>Remboursement € 500</span><span class="text-gray-500">02/11/2025</span></li>
          </ul>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class CreditComponent {

}
