import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScheduledOrderFormComponent } from '../scheduled-order-form/scheduled-order-form.component';
import { ScheduledOrdersListComponent } from '../scheduled-orders-list/scheduled-orders-list.component';
import { ScheduledOrdersStateService } from '../../../services/Order/scheduled-orders-state.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-scheduled-orders-page',
  standalone: true,
  imports: [CommonModule, ScheduledOrderFormComponent, ScheduledOrdersListComponent],
  templateUrl: './scheduled-orders-page.component.html',
  styleUrls: ['./scheduled-orders-page.component.css']
})
export class ScheduledOrdersPageComponent implements OnInit {
  playerId = 0;
  activeTab: 'list' | 'form' = 'list';

  constructor(
    private stateService: ScheduledOrdersStateService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.playerId = this.authService.getCurrentPlayerId() || 0;
    // Ecouter l'evenement de creation d'ordre
    this.stateService.getOrderCreated$().subscribe(() => {
      console.log('Ordre cree! Basculement vers la liste...');
      this.switchTab('list');
    });
  }

  switchTab(tab: 'list' | 'form'): void {
    this.activeTab = tab;
  }
}
