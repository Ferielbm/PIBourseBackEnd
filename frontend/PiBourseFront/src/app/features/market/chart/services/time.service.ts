import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { Timeframe, getTimeframeDuration } from '../models/timeframe.model';
import { TimeAcceleratorService } from '../../../../services/Market/time-accelerator.service';
import { SimulationTimeService } from '../../../../services/Market/simulation-time.service';

const SIMULATION_START_ISO = '2023-01-01T00:00:00Z';
const SIMULATION_END_ISO = '2023-12-31T23:59:59Z';

@Injectable({ providedIn: 'root' })
export class TimeService implements OnDestroy {
  private readonly startDate = new Date(SIMULATION_START_ISO);
  private readonly endDate = new Date(SIMULATION_END_ISO);

  private readonly timeSubject = new BehaviorSubject<Date>(new Date(SIMULATION_START_ISO));
  readonly currentTime$ = this.timeSubject.asObservable();

  private playbackSpeed = 1;
  private playing = false;
  private activeTimeframe: Timeframe = 'M1';

  private timeSubscription?: Subscription;
  private activeSubscription?: Subscription;

  constructor(
    private readonly accelerator: TimeAcceleratorService,
    private readonly simulationTime: SimulationTimeService
  ) {
    // Utiliser le service local en premier pour garantir le bon point de départ
    this.timeSubject.next(this.simulationTime.getCurrentSimDate());
    
    // S'abonner au temps local (prioritaire)
    this.simulationTime.currentSimDateTime$.subscribe(date => {
      this.timeSubject.next(date);
    });
    
    // Essayer le backend en arrière-plan seulement
    this.bootstrapFromBackend();

    // S'abonner au ratio de compression pour ajuster la vitesse locale
    this.accelerator.getCompressionRatio().subscribe(ratio => {
      if (!ratio || ratio <= 0) return;
      // ratio = game seconds per real second -> convertir en minutes per real second
      const minutesPerSecond = Math.max(1, Math.round(ratio / 60));
      this.simulationTime.setSimulationSpeed(minutesPerSecond);
      this.playbackSpeed = minutesPerSecond;
    });
  }

  ngOnDestroy(): void {
    this.timeSubscription?.unsubscribe();
    this.activeSubscription?.unsubscribe();
  }

  getCurrentSimulationTime(): Date {
    return new Date(this.timeSubject.value);
  }

  isPlaying(): boolean {
    return this.playing;
  }

  getPlaybackSpeed(): number {
    return this.playbackSpeed;
  }

  play(timeframe: Timeframe = this.activeTimeframe): void {
    this.activeTimeframe = timeframe;
    this.accelerator.startAcceleration().pipe(
      finalize(() => {
        this.playing = true;
      })
    ).subscribe({
      error: () => {
        this.playing = true; // fallback to local simulation
        this.simulationTime.startSimulation();
      }
    });
  }

  pause(): void {
    this.accelerator.stopAcceleration().pipe(
      finalize(() => {
        this.playing = false;
        this.simulationTime.stopSimulation();
      })
    ).subscribe({
      error: () => {
        this.playing = false;
      }
    });
  }

  reset(): void {
    this.accelerator.resetAcceleration().pipe(
      finalize(() => {
        this.simulationTime.resetSimulation();
        this.timeSubject.next(new Date(SIMULATION_START_ISO));
      })
    ).subscribe({
      error: () => {
        this.simulationTime.resetSimulation();
        this.timeSubject.next(new Date(SIMULATION_START_ISO));
      }
    });
  }

  setPlaybackSpeed(speed: number): void {
    this.playbackSpeed = Math.max(1, speed);
    this.simulationTime.setSimulationSpeed(this.playbackSpeed);
  }

  stepForward(timeframe: Timeframe): void {
    this.simulationTime.advanceTime(this.durationToMinutes(timeframe));
  }

  stepBackward(timeframe: Timeframe): void {
    this.simulationTime.advanceTime(-this.durationToMinutes(timeframe));
  }

  private bootstrapFromBackend(): void {
    // S'abonner au backend seulement pour l'état actif, pas pour le temps
    // Le temps est géré par le service local pour éviter les conflits
    this.activeSubscription = this.accelerator.isAccelerationActive().subscribe({
      next: (active) => {
        this.playing = active;
        if (active) {
          this.simulationTime.startSimulation();
        } else {
          this.simulationTime.stopSimulation();
        }
      },
      error: () => {
        // Ignorer les erreurs du backend
        console.log('Backend time service non disponible, utilisation du service local');
        this.playing = false;
      }
    });
  }

  private durationToMinutes(timeframe: Timeframe): number {
    const durationMs = getTimeframeDuration(timeframe);
    return Math.max(1, Math.round(durationMs / 60000));
  }
}
