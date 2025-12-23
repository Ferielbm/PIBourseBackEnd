import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ScheduledOrderRefreshService {
  private refreshSource = new Subject<void>();
  
  // Observable que les composants peuvent écouter
  refresh$ = this.refreshSource.asObservable();
  
  // Méthode pour déclencher un rafraîchissement
  triggerRefresh(): void {
    this.refreshSource.next();
  }
}
