import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Client } from '@stomp/stompjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient: Client | null = null;
  private connectionStatus = new BehaviorSubject<boolean>(false);
  private subscriptions = new Map<string, any>();

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.stompClient?.active) {
        resolve();
        return;
      }

      try {
        this.stompClient = new Client({
          brokerURL: 'ws://localhost:8084/ws',
          connectHeaders: {},
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          reconnectDelay: 5000,
          debug: (msg) => console.log('STOMP DEBUG:', msg),
          onConnect: () => {
            console.log('✅ WebSocket connecté');
            this.connectionStatus.next(true);
            resolve();
          },
          onStompError: (error: any) => {
            console.error('❌ Erreur connexion WebSocket:', error);
            this.connectionStatus.next(false);
            reject(error);
          },
          onDisconnect: () => {
            console.log('✅ WebSocket déconnecté');
            this.connectionStatus.next(false);
          }
        });
        
        this.stompClient.activate();
      } catch (err) {
        console.error('❌ Erreur initialisation WebSocket:', err);
        reject(err);
      }
    });
  }

  disconnect(): void {
    if (this.stompClient?.active) {
      this.stompClient.deactivate();
    }
  }

  subscribe(destination: string, callback: (message: any) => void): () => void {
    if (!this.stompClient?.active) {
      this.connect().then(() => this.subscribe(destination, callback));
      return () => {};
    }

    const subscription = this.stompClient.subscribe(destination, (message: any) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (err) {
        console.error('Erreur parsing message:', err, message.body);
      }
    });

    this.subscriptions.set(destination, subscription);

    return () => {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
    };
  }

  unsubscribe(destination: string): void {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  isConnected(): boolean {
    return this.connectionStatus.value;
  }

  getConnectionStatus() {
    return this.connectionStatus.asObservable();
  }
}
