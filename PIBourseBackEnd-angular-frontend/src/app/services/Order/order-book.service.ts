import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../environment/environment';

export interface OrderBookSnapshot {
  bids: { [price: string]: number };
  asks: { [price: string]: number };
  lastPrice: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class OrderBookService {
  private baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  /**
   * Récupérer le snapshot du carnet d'ordres pour un symbole
   */
  getOrderBook(playerId: number, symbol: string): Observable<OrderBookSnapshot> {
    return this.http.get<OrderBookSnapshot>(
      `${this.baseUrl}/players/${playerId}/orderbooks/${symbol}`
    );
  }

  /**
   * Récupérer TOUS les ordres depuis la BDD et construire le carnet
   */
  getOrderBookFromDatabase(symbol: string): Observable<OrderBookSnapshot> {
    console.log(`🔍 [OrderBookService] Chargement pour symbole: ${symbol}`);
    
    // D'abord récupérer les stocks pour avoir la correspondance symbol <-> stockId
    return this.http.get<any[]>(`${this.baseUrl}/market/stocks`).pipe(
      switchMap((stocks: any[]) => {
        console.log(`📦 [OrderBookService] ${stocks.length} stocks chargés`);
        
        // Trouver le stock correspondant au symbole
        const targetStock = stocks.find(s => s.symbol === symbol);
        if (!targetStock) {
          console.error(`❌ [OrderBookService] Stock introuvable pour symbole: ${symbol}`);
          return new Observable<OrderBookSnapshot>(observer => {
            observer.next({ bids: {}, asks: {}, lastPrice: null });
            observer.complete();
          });
        }
        
        console.log(`✅ [OrderBookService] Stock trouvé: ${targetStock.symbol}`);
        console.log(`🔍 [OrderBookService] Backend ne renvoie pas d'ID - match par SYMBOL`);
        
        // Maintenant charger les ordres
        return this.http.get<any[]>(`${this.baseUrl}/allorders`).pipe(
          map((allOrders: any[]) => {
            console.log(`📦 [OrderBookService] Total ordres récupérés: ${allOrders.length}`);
            
            // Filtrer par SYMBOL (backend ne renvoie pas d'ID dans /market/stocks)
            const orders = allOrders.filter(o => {
              const orderSymbol = o.stock?.symbol || o.symbol;
              return orderSymbol === symbol;
            });
            
            console.log(`✅ [OrderBookService] Ordres filtrés pour ${symbol}: ${orders.length}`);
        
        // Compter les ordres par statut
        const statusCount: any = {};
        orders.forEach(o => {
          statusCount[o.status] = (statusCount[o.status] || 0) + 1;
        });
        console.log(`📊 [OrderBookService] Répartition par statut:`, statusCount);
        
        // Log détaillé des ordres
        orders.slice(0, 10).forEach((o, i) => {
          console.log(`  Order ${i+1}: ${o.side} ${o.quantity} @ ${o.price} (status: ${o.status}, symbol: ${o.stock?.symbol})`);
        });

        // Grouper par prix
        const bids: { [price: string]: number } = {};
        const asks: { [price: string]: number } = {};
        let lastPrice: number | null = null;

        orders.forEach(order => {
          let price = order.price?.toString();
          const qty = parseFloat(order.remainingQuantity || order.quantity || 0);

            // 🔧 Pour les ordres MARKET ou sans prix
            if (!price || price === 'null' || parseFloat(price) <= 0) {
              // Utiliser le lastPrice du stock cible
              if (targetStock.lastPrice && targetStock.lastPrice > 0) {
                price = targetStock.lastPrice.toString();
                console.log(`🔧 [OrderBookService] Ordre MARKET ${order.side} ${qty} @ targetStock.lastPrice=${price}`);
              } else {
                console.log(`⚠️ [OrderBookService] Ordre MARKET sans prix - on utilise placeholder`);
                price = order.side === 'BUY' ? '999999' : '0.01';
              }
            }          if (order.side === 'BUY') {
            bids[price] = (bids[price] || 0) + qty;
            console.log(`  ➕ BID ajouté: ${price} -> ${bids[price]}`);
          } else if (order.side === 'SELL') {
            asks[price] = (asks[price] || 0) + qty;
            console.log(`  ➕ ASK ajouté: ${price} -> ${asks[price]}`);
          }

          // Utiliser le prix du dernier ordre comme référence
          if (!lastPrice && price && parseFloat(price) < 900000) {
            lastPrice = parseFloat(price);
          }
        });
        
        console.log(`📊 [OrderBookService] Résultat final - Bids: ${Object.keys(bids).length}, Asks: ${Object.keys(asks).length}`);

            return {
              bids,
              asks,
              lastPrice: targetStock.lastPrice || lastPrice
            };
          })
        );
      })
    );
  }
}
