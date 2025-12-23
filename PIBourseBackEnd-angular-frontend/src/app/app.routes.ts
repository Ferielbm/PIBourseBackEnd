import { Routes } from '@angular/router';
import { PlayerComponent } from './features/player/player.component';
import { MarketComponent } from './features/market/market.component';
import {  OrdersComponent } from './features/orders/order/order.component';
import { PortfolioComponent } from './features/portfolio/portfolio.component';
import { CreditComponent } from './features/credit/credit.component';
import { ScheduledOrdersPageComponent } from './features/orders/scheduled-orders-page/scheduled-orders-page.component';
import { AlertMarketComponent } from './features/orders/alert-market/alert-market.component';
import { DecisionTicketsComponent } from './features/orders/decision-tickets/decision-tickets.component';
import { NotificationsListComponent } from './features/notifications/notifications-list/notifications-list.component';
import { GameMasterComponent } from './features/game-master/game-master.component';
import { PriceHistoryComponent } from './features/game-master/price-history/price-history.component';
import { AuthComponent } from './features/auth/auth.component';
import { ShellComponent } from './layout/shell/shell.component';
import { authGuard, gameMasterGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: AuthComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'player', component: PlayerComponent },
      { path: 'market', component: MarketComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'portfolio', component: PortfolioComponent },
      { path: 'credit', component: CreditComponent },
      { path: 'scheduled-orders', component: ScheduledOrdersPageComponent },
      { path: 'alert-market', component: AlertMarketComponent },
      { path: 'decision-tickets', component: DecisionTicketsComponent },
      { path: 'notifications', component: NotificationsListComponent },
      { path: 'game-master', component: GameMasterComponent, canActivate: [gameMasterGuard] },
      { path: 'game-master/price-history', component: PriceHistoryComponent, canActivate: [gameMasterGuard] },
    ]
  },
  { path: '**', redirectTo: 'login' }
];
