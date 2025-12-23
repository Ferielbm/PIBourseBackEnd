import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

const SIMULATION_START_ISO = '2023-01-01T00:00:00';
const SIMULATION_END_ISO = '2023-12-31T16:00:00';
// Point de départ par défaut (on démarre la simulation dès le 1er janvier 2023)
const DEFAULT_SIMULATION_START_ISO = '2023-01-01T09:30:00';

@Injectable({
  providedIn: 'root'
})
export class SimulationTimeService {
  private readonly simulationStart = new Date(SIMULATION_START_ISO);
  private readonly simulationEnd = new Date(SIMULATION_END_ISO);
  private readonly defaultStart = new Date(DEFAULT_SIMULATION_START_ISO);

  private currentSimDateTime = new BehaviorSubject<Date>(new Date(DEFAULT_SIMULATION_START_ISO));
  
  // Observable pour que les composants puissent s'abonner
  currentSimDateTime$ = this.currentSimDateTime.asObservable();
  
  // Vitesse de simulation en minutes simulés par seconde (1 = 1 minute simulée par seconde)
  private simulationSpeed = 1;
  private intervalId?: any;
  private isRunning = false;

  constructor() {
    // Toujours commencer au 01/01/2023 09:30 pour une expérience cohérente
    // Effacer toute date sauvegardée précédemment
    localStorage.removeItem('simDate');
    this.updateSimDate(new Date(this.defaultStart));
  }

  /**
   * Obtenir la date/heure de simulation actuelle
   */
  getCurrentSimDate(): Date {
    return new Date(this.currentSimDateTime.value);
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
    const lowerBound = this.simulationStart.getTime();
    const upperBound = this.simulationEnd.getTime();

    let clamped = newDate.getTime();
    if (clamped < lowerBound) {
      clamped = lowerBound;
    }
    if (clamped > upperBound) {
      clamped = upperBound;
    }

    const nextValue = new Date(clamped);
    this.currentSimDateTime.next(nextValue);
    localStorage.setItem('simDate', nextValue.toISOString());
  }

  /**
   * Définir la date/heure de simulation (publique)
   */
  setCurrentSimDate(date: Date): void {
    // Avoid redundant updates to prevent external loops that override local progression
    const current = this.getCurrentSimDate();
    if (current && current.getTime() === new Date(date).getTime()) {
      return;
    }
    this.updateSimDate(new Date(date));
  }

  /**
   * Démarrer la simulation temporelle automatique
   */
  startSimulation(): void {
    if (this.isRunning) return;
    
    this.isRunning = true;
    // Advance simulation by `simulationSpeed` minutes every 1000ms (1 second)
    this.intervalId = setInterval(() => {
      try {
        this.advanceTime(this.simulationSpeed);
        // debug log every tick (will be noisy) - useful to confirm the timer runs
        // You can comment this out after debugging
        // console.debug('[SimulationTime] tick:', this.getCurrentSimDate());
      } catch (e) {
        console.error('[SimulationTime] tick error:', e);
      }
    }, 1000);
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
    const resetDate = new Date(this.defaultStart);
    this.currentSimDateTime.next(resetDate);
    localStorage.setItem('simDate', resetDate.toISOString());
  }

  /**
   * Changer la vitesse de simulation
   */
  setSimulationSpeed(speed: number): void {
    // speed = minutes simulated per second
    if (!isFinite(speed) || speed <= 0) return;
    this.simulationSpeed = Math.max(1, Math.round(speed));
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
    let start = new Date(end);

    switch(timeframe) {
      case '1M':
        start.setDate(start.getDate() - 30);
        break;
      case '3M':
        start.setMonth(start.getMonth() - 3);
        break;
      case '6M':
        start.setMonth(start.getMonth() - 6);
        break;
      case 'YTD':
        start = new Date(end.getFullYear(), 0, 1);
        break;
      default:
        start.setDate(start.getDate() - 30);
    }

    if (start.getTime() < this.simulationStart.getTime()) {
      start = new Date(this.simulationStart);
    }
    if (end.getTime() > this.simulationEnd.getTime()) {
      end.setTime(this.simulationEnd.getTime());
    }

    return { start, end };
  }

  getSimulationBounds(): { start: Date; end: Date; defaultStart: Date } {
    return {
      start: new Date(this.simulationStart),
      end: new Date(this.simulationEnd),
      defaultStart: new Date(this.defaultStart)
    };
  }
}
