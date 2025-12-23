import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="max-w-5xl mx-auto space-y-6">
      <div class="grid md:grid-cols-3 gap-4">
        <div class="md:col-span-2 rounded-2xl p-6 bg-gradient-to-br from-indigo-600 to-fuchsia-600 text-white shadow-lg">
          <div class="flex items-start justify-between">
            <div>
              <div class="text-sm opacity-80">Solde Wallet (EUR)</div>
              <div class="text-4xl font-extrabold tracking-tight">€ 12,450.32</div>
            </div>
            <div class="px-3 py-1 rounded-full bg-white/15 text-xs">Compte vérifié</div>
          </div>
          <div class="mt-6 grid grid-cols-3 gap-3 text-sm">
            <div>
              <div class="opacity-80">Dépôt total</div>
              <div class="font-semibold">€ 8,000</div>
            </div>
            <div>
              <div class="opacity-80">PNL non réalisé</div>
              <div class="font-semibold">+€ 1,120</div>
            </div>
            <div>
              <div class="opacity-80">Disponibles</div>
              <div class="font-semibold">€ 3,330.32</div>
            </div>
          </div>
        </div>
        <div class="rounded-2xl p-6 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800">
          <div class="font-semibold mb-4">Actions rapides</div>
          <div class="grid grid-cols-2 gap-2">
            <button class="px-3 py-2 rounded-md bg-indigo-600 text-white hover:opacity-90">Déposer</button>
            <button class="px-3 py-2 rounded-md bg-gray-200 dark:bg-gray-800 hover:opacity-90">Retirer</button>
            <button class="col-span-2 px-3 py-2 rounded-md bg-emerald-600 text-white hover:opacity-90">Acheter crypto</button>
          </div>
        </div>
      </div>

      <div class="rounded-2xl p-6 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800">
        <div class="font-semibold mb-4">Informations personnelles</div>
        <div class="grid md:grid-cols-2 gap-4 text-sm">
          <div class="space-y-1">
            <div class="text-gray-500 dark:text-gray-400">Nom</div>
            <div class="font-medium">Doe</div>
          </div>
          <div class="space-y-1">
            <div class="text-gray-500 dark:text-gray-400">Prénom</div>
            <div class="font-medium">John</div>
          </div>
          <div class="space-y-1">
            <div class="text-gray-500 dark:text-gray-400">Email</div>
            <div class="font-medium">john.doe&#64;example.com</div>
          </div>
          <div class="space-y-1">
            <div class="text-gray-500 dark:text-gray-400">Téléphone</div>
            <div class="font-medium">+33 6 12 34 56 78</div>
          </div>
          <div class="space-y-1 md:col-span-2">
            <div class="text-gray-500 dark:text-gray-400">Adresse</div>
            <div class="font-medium">10 Rue de la Bourse, 75002 Paris, France</div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class PlayerComponent {

}
