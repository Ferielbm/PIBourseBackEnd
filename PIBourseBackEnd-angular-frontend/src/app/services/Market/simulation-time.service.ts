import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SimulationTimeService {
  // Date de simulation actuelle (commence le 1er janvier 2023 à 9h30)
  private currentSimDateTime = new BehaviorSubject<Date>(new Date('2023-01-01T09:30:00'));
  
  // Observable pour que les composants puissent s'abonner
  currentSimDateTime$ = this.currentSimDateTime.asObservable();
  
  // Vitesse de simulation (1 = temps réel, 60 = 1 seconde réelle = 1 minute simulée)
  private simulationSpeed = 60;
  private intervalId?: any;
  private isRunning = false;

  constructor() {
    // Charger la date sauvegardée si elle existe
    const savedDate = localStorage.getItem('simDate');
    if (savedDate) {
      this.currentSimDateTime.next(new Date(savedDate));
    } else {
      // Démarrer au 1er février pour avoir 30 jours d'historique disponible
      this.currentSimDateTime.next(new Date('2023-02-01T09:30:00'));
    }
  }

  /**
   * Obtenir la date/heure de simulation actuelle
   */
  getCurrentSimDate(): Date {
    return this.currentSimDateTime.value;
  }

  /**
   * Avancer le temps de X minutes
   */
  advanceTime(minutes: number): void {
    const current = this.getCurrentSimDate();
    current.setMinutes(current.getMinutes() + minutes);
    this.updateSimDate(current);
  }

  /**
   * Avancer le temps de X jours
   */
  advanceDays(days: number): void {
    const current = this.getCurrentSimDate();
    current.setDate(current.getDate() + days);
    this.updateSimDate(current);
  }

  /**
   * Mettre à jour la date de simulation
   */
  private updateSimDate(newDate: Date): void {
    // Ne pas dépasser le 31 décembre 2023
    if (newDate.getFullYear() > 2023) {
      newDate = new Date('2023-12-31T16:00:00');
    }
    
    this.currentSimDateTime.next(newDate);
    localStorage.setItem('simDate', newDate.toISOString());
  }

  /**
   * Démarrer la simulation temporelle automatique
   */
  startSimulation(): void {
    if (this.isRunning) return;
    
    this.isRunning = true;
    this.intervalId = setInterval(() => {
      this.advanceTime(1); // Avance de 1 minute simulée chaque seconde
    }, 1000 / this.simulationSpeed);
  }

  /**
   * Arrêter la simulation temporelle
   */
  stopSimulation(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = undefined;
    }
    this.isRunning = false;
  }

  /**
   * Réinitialiser la simulation au début (1er février pour avoir l'historique)
   */
  resetSimulation(): void {
    this.stopSimulation();
    this.currentSimDateTime.next(new Date('2023-02-01T09:30:00'));
    localStorage.removeItem('simDate');
  }

  /**
   * Changer la vitesse de simulation
   */
  setSimulationSpeed(speed: number): void {
    this.simulationSpeed = speed;
    if (this.isRunning) {
      this.stopSimulation();
      this.startSimulation();
    }
  }

  /**
   * Obtenir la plage de dates pour le graphique selon le timeframe
   * Compatible avec les données journalières
   */
  getChartDateRange(timeframe: string): { start: Date, end: Date } {
    const end = this.getCurrentSimDate();
    const start = new Date(end);
    
    switch(timeframe) {
      case '1M':
        start.setDate(start.getDate() - 30); // 1 mois (30 jours)
        break;
      case '3M':
        start.setMonth(start.getMonth() - 3); // 3 mois
        break;
      case '6M':
        start.setMonth(start.getMonth() - 6); // 6 mois
        break;
      case 'YTD':
        start.setFullYear(2023, 0, 1); // Year To Date (depuis début 2023)
        break;
      default:
        start.setDate(start.getDate() - 30); // Par défaut 1 mois
    }
    
    return { start, end };
  }
}
