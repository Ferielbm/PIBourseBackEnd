import { Routes } from '@angular/router';
import { PlayerComponent } from './features/player/player.component';
import { MarketComponent } from './features/market/market.component';
import { OrdersComponent } from './features/orders/orders.component';
import { PortfolioComponent } from './features/portfolio/portfolio.component';
import { CreditComponent } from './features/credit/credit.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'market' },
  { path: 'player', component: PlayerComponent },
  { path: 'market', component: MarketComponent },
  { path: 'orders', component: OrdersComponent },
  { path: 'portfolio', component: PortfolioComponent },
  { path: 'credit', component: CreditComponent },
  { path: '**', redirectTo: 'market' }
];
