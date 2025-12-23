import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
  HostListener,
  ViewEncapsulation
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { SafeSvgPipe } from './pipes/safe-svg.pipe';

import { Subscription, Subject, BehaviorSubject, Observable, fromEvent } from 'rxjs';
import { debounceTime, filter, takeUntil, tap } from 'rxjs/operators';

import { ChartDataService, ChartSeriesPayload } from './services/chart-data.service';
import { Timeframe, TIMEFRAME_ORDER, getTimeframeDuration } from './models/timeframe.model';
import { TimeService } from './services/time.service';
import { DrawingObject, DrawingPoint, DrawingTool } from './models/drawing.model';
import { DrawingService } from './services/drawing.service';
import { SimulationTimeService } from '../../../services/Market/simulation-time.service';

interface LightweightChartsModule {
  createChart: any;
  ColorType: any;
  CrosshairMode: any;
}

@Component({
  selector: 'app-market-chart',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatSelectModule,
    MatInputModule,
    MatTabsModule,
    MatTooltipModule,
    MatIconModule,
    SafeSvgPipe
  ],
  template: `
    <div class="market-chart" [class.market-chart--loading]="loading">
      <header class="market-chart__header">
        <div class="market-chart__symbol">
          <span class="market-chart__symbol-label">{{ symbol }}</span>
          <span class="market-chart__date-range">{{ currentRangeLabel }}</span>
          <span class="market-chart__status" *ngIf="loading">Chargement…</span>
          <span class="market-chart__error" *ngIf="error">{{ error }}</span>
        </div>
        <div class="market-chart__mode-toggle">
          <button type="button"
                  class="market-chart__mode-btn"
                  [class.market-chart__mode-btn--active]="displayMode === 'candles'"
                  (click)="setDisplayMode('candles')"
                  title="Bougies">
            <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
              <path d="M11 5V2h2v3h3v2h-3v10h3v2h-3v3h-2v-3H8v-2h3V7H8V5h3z" fill="currentColor"></path>
            </svg>
          </button>
          <button type="button"
                  class="market-chart__mode-btn"
                  [class.market-chart__mode-btn--active]="displayMode === 'line'"
                  (click)="setDisplayMode('line')"
                  title="Courbe">
            <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
              <path d="m5 17 5-6 4 4 5-7 1.5 1-6.5 9-4.2-4.2L6 19z" fill="currentColor"></path>
            </svg>
          </button>
        </div>
      </header>

      <div class="market-chart__toolbar">
        <div class="market-chart__timeframes">
          <button
            *ngFor="let tf of timeframes"
            class="market-chart__timeframe-btn"
            [class.market-chart__timeframe-btn--active]="selectedTimeframe === tf"
            (click)="selectTimeframe(tf)"
          >
            {{ tf }}
          </button>
        </div>
        <div class="market-chart__playback">
          <button type="button" (click)="stepBackward()" title="Reculer"><span>&lt;</span></button>
          <button type="button" (click)="togglePlayback()" title="Lecture/Pause">
            <span *ngIf="!isPlaying">▶</span>
            <span *ngIf="isPlaying">❚❚</span>
          </button>
          <button type="button" (click)="stepForward()" title="Avancer"><span>&gt;</span></button>
          <div class="market-chart__speed">
            <label for="speedSelect">x{{ playbackSpeed }}</label>
            <input id="speedSelect" type="range" min="1" max="20" step="1" [(ngModel)]="playbackSpeed" (change)="updatePlaybackSpeed()" />
          </div>
        </div>
        <div class="market-chart__time-indicator">
          <span>{{ simulationTime | date:'dd/MM/yyyy HH:mm' }}</span>
        </div>
      </div>

      <div class="market-chart__body">
        <aside class="market-chart__left-toolbar">
          <button
            *ngFor="let tool of drawingTools"
            type="button"
            class="market-chart__tool-btn"
            [class.market-chart__tool-btn--active]="activeTool === tool.id"
            (click)="selectTool(tool.id)"
            [title]="tool.label"
          >
            <svg [innerHTML]="tool.icon | safeSvg"></svg>
          </button>
          <button type="button"
                  class="market-chart__tool-btn"
                  [class.market-chart__tool-btn--active]="magnetEnabled"
                  (click)="toggleMagnet()"
                  title="Mode aimant">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path d="M6 17h2v3a3 3 0 1 0 6 0v-3h2a5 5 0 0 0 5-5V5a3 3 0 0 0-3-3h-4a3 3 0 0 0-3 3v10a1 1 0 0 0 2 0V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v7a3 3 0 0 1-3 3h-2V7h-2v13a1 1 0 0 1-2 0v-3H6z" fill="currentColor"></path>
            </svg>
          </button>
          <button type="button"
                  class="market-chart__tool-btn"
                  (click)="lockSelection(true)"
                  title="Verrouiller la sélection">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path d="M7 10V7a5 5 0 0 1 10 0v3h1a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h1zm2 0h6V7a3 3 0 0 0-6 0v3z" fill="currentColor"></path>
            </svg>
          </button>
          <button type="button"
                  class="market-chart__tool-btn"
                  (click)="lockSelection(false)"
                  title="Déverrouiller la sélection">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path d="M7 11H6a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2H7zm4 0v-1a3 3 0 0 1 6 0h2a5 5 0 0 0-10 0v1z" fill="currentColor"></path>
            </svg>
          </button>
          <button type="button"
                  class="market-chart__tool-btn"
                  (click)="removeSelection()"
                  title="Supprimer">
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path d="M6 6h12l-1 14H7L6 6zm5 2v10h2V8h-2zm4 0v10h2V8h-2zM9 8v10h2V8H9zm2-5h2l1 1h6v2H4V4h6l1-1z" fill="currentColor"></path>
            </svg>
          </button>
        </aside>

        <div class="market-chart__canvas-wrapper"
             #chartHost>
          <div class="market-chart__canvas" #chartContainer></div>
          <svg #overlaySvg
               class="market-chart__overlay"
               (pointerdown)="onPointerDown($event)"
               (pointermove)="onPointerMove($event)"
               (pointerup)="onPointerUp($event)"
               (pointerleave)="onPointerLeave($event)"></svg>
          <div class="market-chart__time-cursor" *ngIf="timeCursorPosition" [style.left.px]="timeCursorPosition"></div>
        </div>
      </div>

      <footer class="market-chart__footer">
        <div class="market-chart__footer-col">
          <span class="market-chart__footer-label">Objets</span>
          <span>{{ drawings.length }}</span>
        </div>
        <div class="market-chart__footer-col">
          <span class="market-chart__footer-label">Mode</span>
          <span>{{ activeTool | titlecase }}</span>
        </div>
        <div class="market-chart__footer-col">
          <span class="market-chart__footer-label">Aimant</span>
          <span>{{ magnetEnabled ? 'ON' : 'OFF' }}</span>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }
    .market-chart {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      background: linear-gradient(145deg, #0b1220, #05070d);
      color: #cbd5f5;
      padding: 1rem 1.25rem 1.5rem;
      border-radius: 1.25rem;
      border: 1px solid rgba(56, 189, 248, 0.08);
      box-shadow: 0 24px 60px rgba(14, 165, 233, 0.15);
      position: relative;
      overflow: hidden;
    }
    .market-chart--loading::after {
      content: '';
      position: absolute;
      inset: 0;
      background: rgba(10, 19, 35, 0.6);
      backdrop-filter: blur(3px);
      pointer-events: none;
    }
    .market-chart__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
    }
    .market-chart__symbol {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 0.95rem;
    }
    .market-chart__symbol-label {
      font-size: 1.35rem;
      font-weight: 700;
      color: #f8fafc;
    }
    .market-chart__date-range {
      font-size: 0.85rem;
      color: #8da2c9;
    }
    .market-chart__status {
      font-size: 0.75rem;
      color: #22d3ee;
    }
    .market-chart__error {
      font-size: 0.75rem;
      color: #f87171;
    }
    .market-chart__mode-toggle {
      display: flex;
      gap: 0.5rem;
    }
    .market-chart__mode-btn {
      width: 2.25rem;
      height: 2.25rem;
      display: grid;
      place-items: center;
      background: rgba(15, 23, 42, 0.6);
      border: 1px solid transparent;
      border-radius: 0.75rem;
      color: #8da2c9;
      transition: all 0.2s ease;
    }
    .market-chart__mode-btn--active,
    .market-chart__mode-btn:hover {
      background: linear-gradient(120deg, rgba(14, 165, 233, 0.15), rgba(56, 189, 248, 0.1));
      border-color: rgba(56, 189, 248, 0.35);
      color: #f8fafc;
    }
    .market-chart__toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      background: rgba(10, 15, 28, 0.8);
      border: 1px solid rgba(56, 189, 248, 0.06);
      border-radius: 0.75rem;
      padding: 0.75rem 1rem;
    }
    .market-chart__timeframes {
      display: flex;
      gap: 0.35rem;
    }
    .market-chart__timeframe-btn {
      padding: 0.35rem 0.75rem;
      font-size: 0.75rem;
      border-radius: 0.5rem;
      border: 1px solid rgba(51, 65, 85, 0.35);
      background: rgba(15, 23, 42, 0.6);
      color: #cbd5f5;
      transition: all 0.2s ease;
    }
    .market-chart__timeframe-btn--active,
    .market-chart__timeframe-btn:hover {
      border-color: rgba(56, 189, 248, 0.45);
      background: rgba(14, 165, 233, 0.18);
      color: #f8fafc;
    }
    .market-chart__playback {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .market-chart__playback button {
      width: 2.2rem;
      height: 2.2rem;
      border-radius: 0.65rem;
      border: 1px solid rgba(51, 65, 85, 0.4);
      background: rgba(15, 23, 42, 0.6);
      color: #e2e8f0;
      display: grid;
      place-items: center;
      font-size: 0.9rem;
      transition: all 0.2s ease;
    }
    .market-chart__playback button:hover {
      border-color: rgba(56, 189, 248, 0.4);
      background: rgba(14, 165, 233, 0.18);
    }
    .market-chart__speed {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      min-width: 120px;
    }
    .market-chart__speed label {
      font-size: 0.75rem;
      color: #93c5fd;
    }
    .market-chart__speed input[type='range'] {
      flex: 1;
      accent-color: #0ea5e9;
    }
    .market-chart__time-indicator {
      font-size: 0.85rem;
      font-variant-numeric: tabular-nums;
      color: #cbd5f5;
      padding: 0.35rem 0.65rem;
      border-radius: 0.5rem;
      background: rgba(148, 163, 184, 0.12);
    }
    .market-chart__body {
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 1rem;
    }
    .market-chart__left-toolbar {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      padding: 0.65rem;
      border-radius: 0.9rem;
      background: rgba(10, 15, 28, 0.9);
      border: 1px solid rgba(30, 64, 175, 0.18);
      max-height: 560px;
      overflow-y: auto;
    }
    .market-chart__tool-btn {
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 0.75rem;
      border: 1px solid transparent;
      background: rgba(15, 23, 42, 0.5);
      color: #8da2c9;
      display: grid;
      place-items: center;
      transition: all 0.2s ease;
    }
    .market-chart__tool-btn svg {
      width: 18px;
      height: 18px;
      fill: currentColor;
    }
    .market-chart__tool-btn--active,
    .market-chart__tool-btn:hover {
      border-color: rgba(56, 189, 248, 0.4);
      background: rgba(14, 116, 233, 0.22);
      color: #f8fafc;
      box-shadow: 0 10px 20px rgba(14, 116, 233, 0.2);
    }
    .market-chart__canvas-wrapper {
      position: relative;
      border-radius: 1rem;
      overflow: hidden;
      border: 1px solid rgba(56, 189, 248, 0.1);
      background: linear-gradient(180deg, rgba(8, 15, 29, 0.95), rgba(2, 6, 12, 0.98));
      min-height: 520px;
    }
    .market-chart__canvas {
      width: 100%;
      height: 520px;
    }
    .market-chart__overlay {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      overflow: visible;
    }
    .market-chart__canvas-wrapper.is-drawing .market-chart__overlay {
      pointer-events: auto;
      cursor: crosshair;
      z-index: 5;
    }
    .market-chart__time-cursor {
      position: absolute;
      top: 0;
      bottom: 0;
      width: 2px;
      background: linear-gradient(180deg, rgba(14, 165, 233, 0.85), rgba(34, 211, 238, 0.35));
      pointer-events: none;
      z-index: 3;
    }
    .market-chart__footer {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0.75rem;
      background: rgba(10, 15, 28, 0.82);
      border: 1px solid rgba(148, 163, 184, 0.12);
      border-radius: 0.75rem;
      padding: 0.75rem 1rem;
      font-size: 0.8rem;
    }
    .market-chart__footer-col {
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: #cbd5f5;
    }
    .market-chart__footer-label {
      font-weight: 500;
      color: #94a3b8;
      letter-spacing: 0.01em;
    }

    @media (max-width: 1280px) {
      .market-chart {
        padding: 1rem;
      }
      .market-chart__body {
        grid-template-columns: 1fr;
      }
      .market-chart__left-toolbar {
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: center;
        max-height: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MarketChartComponent implements OnInit, AfterViewInit, OnDestroy {
  // Inputs
  @Input() 
  set symbol(value: string) {
    if (value && value !== this.currentSymbolInternal) {
      this.currentSymbolInternal = value;
      this.onSymbolChange();
    }
  }
  get symbol(): string {
    return this.currentSymbolInternal;
  }
  
  @Input() height = 400;
  @Input() showVolume = true;
  @Input() showDrawings = true;
  @Input() initialBarsCount = 100;

  // View Children
  @ViewChild('chartContainer', { static: true }) private chartContainer!: ElementRef<HTMLDivElement>;
  @ViewChild('chartHost', { static: true }) private chartHost!: ElementRef<HTMLDivElement>;
  @ViewChild('overlaySvg', { static: true }) private overlaySvg!: ElementRef<SVGSVGElement>;

  // Public properties
  readonly timeframes = TIMEFRAME_ORDER;
  readonly drawingTools: { id: DrawingTool; label: string; icon: string }[] = [
    { id: 'cursor', label: 'Sélection', icon: '<svg viewBox="0 0 24 24"><path d="M4 3l6.4 16 2.4-6.5L19 11 4 3z"/></svg>' },
    { id: 'crosshair', label: 'Crosshair', icon: '<svg viewBox="0 0 24 24"><path d="M11 3h2v7h7v2h-7v7h-2v-7H4v-2h7z"/></svg>' },
    { id: 'vertical-line', label: 'Ligne verticale', icon: '<svg viewBox="0 0 24 24"><path d="M11 4h2v16h-2z"/></svg>' },
    { id: 'horizontal-line', label: 'Ligne horizontale', icon: '<svg viewBox="0 0 24 24"><path d="M4 11h16v2H4z"/></svg>' },
    { id: 'trendline', label: 'Ligne de tendance', icon: '<svg viewBox="0 0 24 24"><path d="M4 18 18 4l2 2L6 20z"/></svg>' },
    { id: 'fibonacci', label: 'Retracement Fibonacci', icon: '<svg viewBox="0 0 24 24"><path d="M4 5h2v14H4zM8 7h2v10H8zm4-2h2v14h-2zm4 3h2v8h-2zm4 4h2v2h-2z"/></svg>' }
  ];

  // Component state
  selectedTimeframe: Timeframe = 'M15';
  displayMode: 'candles' | 'line' = 'candles';
  loading = false;
  error: string | null = null;
  simulationTime: Date = new Date('2023-01-01T00:00:00Z');
  currentRangeLabel = '';
  isPlaying = false;
  playbackSpeed = 1;
  magnetEnabled = true;
  drawings: DrawingObject[] = [];
  activeTool: DrawingTool = 'cursor';
  selectedDrawingId: string | null = null;
  timeCursorPosition: number | null = null;

  // Private properties
  private lwCharts!: LightweightChartsModule;
  private chart: any;
  private candleSeries: any;
  private lineSeries: any;
  private volumeSeries: any;
  private timeScale: any;
  private isDragging = false;
  private lastDragX = 0;
  private currentRange = { from: 0, to: 0, barSpacing: 6, minBarSpacing: 2, maxBarSpacing: 20 };
  private visibleBars = 100;
  private subscriptions = new Subscription();
  private destroy$ = new Subject<void>();
  private selectedDrawing: DrawingObject | null = null;
  private isDrawing = false;
  private currentPoints: DrawingPoint[] = [];
  private draftDrawing: DrawingObject | null = null;
  private resizeObserver?: ResizeObserver;
  private timeSubscription?: Subscription;
  private drawingSubscription?: Subscription;
  private chartUpdateSubscription?: Subscription;
  private seriesPayload: ChartSeriesPayload | null = null;
  private isPointerDown = false;
  private lastLoadedTime: Date | null = null;
  private currentSymbolInternal = 'AAPL';
  private timeUpdateInterval?: any;

  constructor(
    private readonly cdr: ChangeDetectorRef,
    private readonly chartDataService: ChartDataService,
    private readonly timeService: TimeService,
    private readonly drawingService: DrawingService,
    private readonly simulationTimeService: SimulationTimeService
  ) {}

  // Lifecycle hooks
  ngOnInit(): void {
    this.setupTimeNavigation();
    this.subscribeToDrawings();
    this.startRealTimeUpdates();
  }

  private setupTimeNavigation(): void {
    this.simulationTimeService.currentSimDateTime$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(date => {
      this.simulationTime = date;
      this.cdr.markForCheck();
      this.updateTimeCursor();
    });
  }

  private subscribeToDrawings(): void {
    if (this.drawingSubscription) {
      this.drawingSubscription.unsubscribe();
    }

    this.drawingSubscription = this.drawingService.drawings$
      .pipe(takeUntil(this.destroy$))
      .subscribe(drawings => {
        this.drawings = drawings;
        this.renderOverlay();
      });
  }

  private updateSeriesVisibility(): void {
    if (!this.candleSeries || !this.lineSeries) {
      return;
    }
    
    if (this.displayMode === 'candles') {
      this.candleSeries.applyOptions({ visible: true });
      this.lineSeries.applyOptions({ visible: false });
    } else {
      this.candleSeries.applyOptions({ visible: false });
      this.lineSeries.applyOptions({ visible: true });
    }
  }

  async ngAfterViewInit(): Promise<void> {
    await this.ensureLibrary();
    await this.initializeChart();
    this.setInteractionMode();
    this.subscribeToTime();
    this.subscribeToDrawings();
    await this.loadSeries();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
    }
    if (this.timeUpdateInterval) {
      clearInterval(this.timeUpdateInterval);
    }
    if (this.chart) {
      this.chart.remove();
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  selectTimeframe(tf: Timeframe): void {
    if (this.selectedTimeframe === tf) {
      return;
    }
    this.selectedTimeframe = tf;
    this.loadSeries();
  }

  setDisplayMode(mode: 'candles' | 'line'): void {
    if (this.displayMode === mode) {
      return;
    }
    this.displayMode = mode;
    this.updateSeriesVisibility();
  }

  togglePlayback(): void {
    if (this.isPlaying) {
      this.simulationTimeService.stopSimulation();
    } else {
      this.simulationTimeService.startSimulation();
    }
    this.isPlaying = !this.isPlaying;
  }

  stepForward(): void {
    this.simulationTimeService.advanceTime(1);
  }

  stepBackward(): void {
    this.simulationTimeService.advanceTime(-1);
  }

  updatePlaybackSpeed(): void {
    // La méthode setSpeed n'existe pas, on utilise la propriété directement
    this.simulationTimeService['simulationSpeed'] = this.playbackSpeed;
  }

  selectTool(tool: DrawingTool): void {
    this.activeTool = tool;
    if (tool === 'crosshair') {
      this.chart.applyOptions({ crosshair: { mode: this.lwCharts.CrosshairMode.Normal } });
    } else {
      this.chart.applyOptions({ crosshair: { mode: this.lwCharts.CrosshairMode.Hidden } });
    }
    this.setInteractionMode();
  }

  toggleMagnet(): void {
    this.magnetEnabled = !this.magnetEnabled;
  }

  lockSelection(lock: boolean): void {
    if (this.selectedDrawingId) {
      this.drawingService.setLocked(this.selectedDrawingId, lock);
    }
  }

  removeSelection(): void {
    if (this.selectedDrawingId) {
      this.drawingService.remove(this.selectedDrawingId);
      this.selectedDrawingId = null;
    }
  }

  onPointerDown(event: PointerEvent): void {
    if (!this.isDrawingTool(this.activeTool)) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    this.isPointerDown = true;
    const point = this.toDrawingPoint(event);
    if (!point) {
      return;
    }
    this.overlaySvg?.nativeElement.setPointerCapture(event.pointerId);
    const id = `drawing-${Date.now()}`;
    this.draftDrawing = {
      id,
      type: this.activeTool,
      points: [point],
      locked: false,
      style: this.drawingService.getDefaultStyle(this.activeTool)
    };
    if (this.activeTool === 'text') {
      this.draftDrawing.label = 'Note';
    }
    this.renderOverlay();
  }

  onPointerMove(event: PointerEvent): void {
    if (!this.isPointerDown || !this.draftDrawing) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    const point = this.toDrawingPoint(event);
    if (!point) {
      return;
    }
    if (this.draftDrawing.points.length === 1) {
      this.draftDrawing.points.push(point);
    } else {
      this.draftDrawing.points[this.draftDrawing.points.length - 1] = point;
    }
    this.renderOverlay();
  }

  onPointerUp(event: PointerEvent): void {
    if (!this.isPointerDown || !this.draftDrawing) {
      this.isPointerDown = false;
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    const point = this.toDrawingPoint(event);
    if (point) {
      if (this.draftDrawing.points.length === 1) {
        this.draftDrawing.points.push(point);
      } else {
        this.draftDrawing.points[this.draftDrawing.points.length - 1] = point;
      }
      this.draftDrawing = this.normalizeDrawing(this.draftDrawing);
      this.drawingService.add(this.draftDrawing);
      this.selectedDrawingId = this.draftDrawing.id;
    }
    this.overlaySvg?.nativeElement.releasePointerCapture(event.pointerId);
    this.draftDrawing = null;
    this.isPointerDown = false;
    this.renderOverlay();
  }

  onPointerLeave(_: PointerEvent): void {
    if (!this.isPointerDown) {
      return;
    }
    this.isPointerDown = false;
    this.draftDrawing = null;
    this.renderOverlay();
  }

  private setInteractionMode(): void {
    const drawingMode = this.isDrawingTool(this.activeTool);
    if (this.overlaySvg) {
      const svg = this.overlaySvg.nativeElement;
      svg.style.pointerEvents = drawingMode ? 'auto' : 'none';
      svg.style.cursor = drawingMode ? 'crosshair' : 'default';
    }
    // Toggle a class on the canvas wrapper so CSS can control overlay behavior
    try {
      const host = this.chartHost?.nativeElement;
      if (host) {
        host.classList.toggle('is-drawing', drawingMode);
      }
    } catch (e) {
      // ignore
    }
    if (this.chart) {
      this.chart.applyOptions({
        handleScroll: {
          mouseWheel: true,
          pressedMouseMove: !drawingMode
        },
        handleScale: {
          axisPressedMouseMove: !drawingMode,
          mouseWheel: true,
          pinch: !drawingMode
        }
      });
    }
  }

  private isDrawingTool(tool: DrawingTool): boolean {
    return tool !== 'cursor' && tool !== 'crosshair';
  }

  private async ensureLibrary(): Promise<void> {
    const win = window as any;
    if (win.LightweightCharts) {
      this.lwCharts = win.LightweightCharts;
      return;
    }
    await new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://unpkg.com/lightweight-charts@4.2.1/dist/lightweight-charts.standalone.production.js';
      script.onload = () => {
        this.lwCharts = win.LightweightCharts;
        resolve();
      };
      script.onerror = () => reject(new Error('Failed to load chart library'));
      document.head.appendChild(script);
    });
  }

  private async initializeChart(): Promise<void> {
    const container = this.chartContainer.nativeElement;
    this.chart = this.lwCharts.createChart(container, {
      layout: {
        background: { type: this.lwCharts.ColorType.Solid, color: '#020617' },
        textColor: '#a5b4fc',
        fontFamily: 'Inter, "JetBrains Mono", system-ui, sans-serif'
      },
      width: container.clientWidth,
      height: 520,
      grid: {
        vertLines: { color: '#1f2937', visible: true },
        horzLines: { color: '#1f2937', visible: true }
      },
      rightPriceScale: {
        borderVisible: false,
        textColor: '#cbd5f5',
        scaleMargins: { top: 0.12, bottom: 0.25 }
      },
      timeScale: {
        borderVisible: false,
        rightOffset: 12,
        barSpacing: 6, // Réduit pour des bougies plus fines
        minBarSpacing: 1,
        lockVisibleTimeRangeOnResize: false,
        timeVisible: true,
        secondsVisible: false
      },
      crosshair: {
        mode: this.lwCharts.CrosshairMode.Normal,
        vertLine: { color: '#38bdf8', labelBackgroundColor: '#0ea5e9' },
        horzLine: { color: '#38bdf8', labelBackgroundColor: '#0ea5e9' }
      },
      localization: {
        dateFormat: 'dd-MM-yyyy'
      },
      handleScroll: {
        mouseWheel: true,
        pressedMouseMove: true
      },
      handleScale: {
        axisPressedMouseMove: true,
        mouseWheel: true,
        pinch: true
      }
    });

    this.candleSeries = this.chart.addCandlestickSeries({
      upColor: '#22c55e',
      downColor: '#ef4444',
      borderUpColor: '#22c55e',
      borderDownColor: '#ef4444',
      wickUpColor: '#22c55e',
      wickDownColor: '#ef4444',
      priceFormat: {
        type: 'price',
        minMove: 0.01,
        precision: 2
      }
    });

    this.lineSeries = this.chart.addLineSeries({
      color: '#38bdf8',
      lineWidth: 2,
      priceLineVisible: false,
      priceFormat: {
        type: 'price',
        minMove: 0.01,
        precision: 2
      }
    });

    this.volumeSeries = this.chart.addHistogramSeries({
      priceFormat: { type: 'volume' },
      priceScaleId: '',
      scaleMargins: { top: 0.8, bottom: 0 },
      color: '#334155',
      priceLineVisible: false,
      lastValueVisible: false
    });

    this.resizeObserver = new ResizeObserver(entries => {
      requestAnimationFrame(() => {
        const entry = entries[entries.length - 1];
        if (!entry) {
          return;
        }
        const width = entry.contentRect.width;
        this.chart.applyOptions({ width });
        this.renderOverlay();
      });
    });
    this.resizeObserver.observe(this.chartHost.nativeElement);

    this.chart.timeScale().subscribeVisibleTimeRangeChange(() => {
      this.renderOverlay();
    });
  }

  private async loadSeries(): Promise<void> {
    if (!this.chart || !this.currentSymbolInternal) {
      return;
    }

    try {
      this.loading = true;
      this.cdr.markForCheck();

      // Utiliser la date de simulation actuelle comme date de fin
      const endDate = this.simulationTimeService.getCurrentSimDate();
      
      // Charger plus d'historique pour avoir le contexte
      const payload = await this.chartDataService.loadSeries(
        this.currentSymbolInternal,
        this.selectedTimeframe,
        endDate,
        500, // Augmenter le nombre de bougies pour plus de données
        true
      );
      
      this.seriesPayload = payload;
      const { candles } = payload;
      const volume = 'volume' in payload ? payload.volume : [];
      
      // Logs de débogage pour voir les données chargées
      console.log(`📊 Loaded ${candles.length} candles for ${this.currentSymbolInternal}`);
      if (candles.length > 0) {
        const firstCandle = new Date(candles[0].time * 1000);
        const lastCandle = new Date(candles[candles.length - 1].time * 1000);
        console.log(`📅 Date range: ${firstCandle.toLocaleDateString()} → ${lastCandle.toLocaleDateString()}`);
        console.log(`🎯 End date used: ${endDate.toLocaleDateString()}`);
      }
      
      // Mettre à jour les séries
      if (this.candleSeries) {
        this.candleSeries.setData(candles);
      }
      
      if (this.volumeSeries && volume) {
        this.volumeSeries.setData(volume);
      }
      
      // Attendre que le graphique soit complètement rendu
      setTimeout(() => {
        // Si c'est le premier chargement (avant que la simulation commence),
        // centrer la vue sur la plage historique Oct 2022 → endDate (généralement 01/01/2023)
        if (!this.lastLoadedTime) {
          try {
            const historicalStartTs = Math.floor(new Date('2022-10-01T00:00:00Z').getTime() / 1000);
            const endTs = Math.floor(endDate.getTime() / 1000);
            const lastIndex = candles.length - 1;

            // Trouver les indices couvrant la plage Oct 2022 → endDate
            let startIndex = candles.findIndex(c => c.time >= historicalStartTs);
            if (startIndex === -1) startIndex = 0;
            let endIndex = candles.findIndex(c => c.time >= endTs);
            if (endIndex === -1) endIndex = lastIndex;
            if (endIndex < startIndex) endIndex = lastIndex;

            const rangeSize = endIndex - startIndex + 1;
            // Déterminer le nombre de barres visibles souhaitées (au moins 100)
            const visibleBarsDesired = Math.min(Math.max(rangeSize + 40, 100), candles.length);
            const padding = Math.max(0, Math.floor((visibleBarsDesired - rangeSize) / 2));

            let visibleFrom = Math.max(0, startIndex - padding);
            let visibleTo = Math.min(lastIndex, visibleFrom + visibleBarsDesired - 1);

            const visibleRange = { from: visibleFrom, to: visibleTo };

            // Ajuster barSpacing pour lisibilité
            this.chart.applyOptions({ timeScale: { barSpacing: 6, minBarSpacing: 1 } });

            if (this.chart.timeScale().setVisibleLogicalRange) {
              this.chart.timeScale().setVisibleLogicalRange(visibleRange);
            } else {
              this.chart.timeScale().setVisibleRange(visibleRange);
            }

            const firstCandle = new Date(candles[startIndex].time * 1000);
            const lastCandle = new Date(candles[endIndex].time * 1000);
            console.log(`📍 Initial historical view: ${firstCandle.toLocaleDateString()} → ${lastCandle.toLocaleDateString()} (centered)`);
          } catch (e) {
            // En cas d'erreur, revenir au comportement par défaut
            this.adjustVisibleRangeToCurrentTime();
          }
        } else {
          // Lors des rechargements normaux, recaler à la date courante (simulation en cours)
          this.adjustVisibleRangeToCurrentTime();
        }

        // Forcer un second ajustement rapide pour s'assurer que le rendu est correct
        setTimeout(() => {
          if (!this.lastLoadedTime) {
            // rien, la vue initiale a déjà été positionnée
          } else {
            this.adjustVisibleRangeToCurrentTime();
          }
        }, 100);
      }, 100);
      
      // Enregistrer le temps chargé
      this.lastLoadedTime = new Date(endDate);
      
    } catch (error) {
      console.error('Error loading series:', error);
      this.error = 'Erreur lors du chargement des données';
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  private subscribeToTime(): void {
    if (this.timeSubscription) {
      this.timeSubscription.unsubscribe();
    }

    this.timeSubscription = this.simulationTimeService.currentSimDateTime$
      .pipe(
        tap(time => {
          this.simulationTime = time;
          this.updateTimeCursor();
          this.cdr.markForCheck();
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();
  }

  private adjustVisibleRange(): void {
    if (!this.chart || !this.seriesPayload) {
      return;
    }

    const { candles } = this.seriesPayload;
    if (!candles || candles.length === 0) {
      return;
    }

    const visibleRange = {
      from: Math.max(0, candles.length - this.visibleBars),
      to: candles.length - 1
    };

    // Utiliser la plage logique (indices de bougies) afin que le timeScale se positionne
    // correctement sur les dernières bougies plutôt que d'interpréter les valeurs
    // comme des timestamps.
    if (this.chart.timeScale().setVisibleLogicalRange) {
      this.chart.timeScale().setVisibleLogicalRange(visibleRange);
    } else {
      this.chart.timeScale().setVisibleRange(visibleRange);
    }
  }

  private adjustVisibleRangeToCurrentTime(): void {
    if (!this.chart || !this.seriesPayload) {
      return;
    }

    const { candles } = this.seriesPayload;
    if (!candles || candles.length === 0) {
      return;
    }

    // Au démarrage, positionner sur janvier 2023 (la bougie la plus récente chargée)
    const lastIndex = candles.length - 1;
    const visibleBars = 100;
    
    // Afficher les 100 dernières bougies, avec la plus récente (janvier 2023) visible à droite
    let startIndex = Math.max(0, lastIndex - visibleBars + 1);
    let endIndex = lastIndex;
    
    const visibleRange = {
      from: startIndex,
      to: endIndex
    };

    // Corriger le zoom excessif en ajustant le barSpacing
    this.chart.applyOptions({
      timeScale: {
        barSpacing: 6, // Forcer un espacement raisonnable
        minBarSpacing: 1
      }
    });

    // Positionner la plage visible en utilisant des indices logiques (bar indices)
    // pour s'assurer que la vue est collée sur la bougie la plus récente.
    if (this.chart.timeScale().setVisibleLogicalRange) {
      this.chart.timeScale().setVisibleLogicalRange(visibleRange);
    } else {
      this.chart.timeScale().setVisibleRange(visibleRange);
    }
    
    const lastCandleTime = new Date(candles[lastIndex].time * 1000);
    console.log(`📍 Chart positioned to January 2023: ${lastCandleTime.toLocaleString()}, showing last ${visibleBars} bars (${startIndex}-${endIndex})`);
  }

  private updateTimeCursor(): void {
    if (!this.chart || !this.seriesPayload) {
      this.timeCursorPosition = null;
      return;
    }
    const time = Math.floor(this.simulationTime.getTime() / 1000);
    const coordinate = this.chart.timeScale().timeToCoordinate(time);
    this.timeCursorPosition = coordinate ?? null;
  }

  /**
   * Gérer le changement de symbole
   */
  private onSymbolChange(): void {
    console.log(`🔄 Symbol changed to: ${this.currentSymbolInternal}`);
    this.lastLoadedTime = null; // Forcer le rechargement complet
    if (this.chart) {
      this.loadSeries(); // Recharger les données du nouveau symbole
    }
  }

  /**
   * Démarrer les mises à jour en temps réel
   */
  private startRealTimeUpdates(): void {
    // Vérifier toutes les 2 secondes si de nouvelles données sont disponibles
    this.timeUpdateInterval = setInterval(() => {
      this.checkForNewData();
    }, 2000);
  }

  /**
   * Vérifier si de nouvelles données sont disponibles et recharger si nécessaire
   */
  private checkForNewData(): void {
    const currentTime = this.simulationTimeService.getCurrentSimDate();
    
    // Recharger si le temps a avancé significativement (plus de 5 minutes) ou si le symbole a changé
    if (!this.lastLoadedTime || 
        Math.abs(currentTime.getTime() - this.lastLoadedTime.getTime()) > 5 * 60 * 1000) {
      this.updateDataWithTime(currentTime);
    }
  }

  /**
   * Mettre à jour les données avec le temps actuel
   */
  private async updateDataWithTime(currentTime: Date): Promise<void> {
    if (!this.chart || this.loading) return;

    try {
      this.loading = true;
      this.cdr.markForCheck();
      
      const payload = await this.chartDataService.loadSeries(
        this.currentSymbolInternal,
        this.selectedTimeframe,
        currentTime,
        this.initialBarsCount,
        true
      );
      
      this.seriesPayload = payload;
      const { candles } = payload;
      
      // Mettre à jour les séries avec animation
      if (this.candleSeries && candles.length > 0) {
        this.candleSeries.setData(candles);
      }
      
      if (this.volumeSeries && payload.volumes && payload.volumes.length > 0) {
        this.volumeSeries.setData(payload.volumes);
      }
      
      // Recentrer seulement si de nouvelles bougies ont été ajoutées (pendant la simulation)
      if (candles.length > 0) {
        const lastCandleTime = new Date(candles[candles.length - 1].time * 1000);
        if (this.lastLoadedTime && lastCandleTime > this.lastLoadedTime) {
          this.adjustVisibleRangeToCurrentTime();
        }
      }
      
      this.lastLoadedTime = new Date(currentTime);
      console.log(`📊 Chart updated for ${this.currentSymbolInternal} at ${currentTime.toLocaleString()}`);
      
    } catch (error) {
      console.error('Error updating chart data:', error);
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  private renderOverlay(): void {
    if (!this.overlaySvg) {
      return;
    }
    const svg = this.overlaySvg.nativeElement;
    const width = this.chartContainer.nativeElement.clientWidth;
    const height = this.chartContainer.nativeElement.clientHeight;
    svg.setAttribute('viewBox', `0 0 ${width} ${height}`);
    svg.innerHTML = '';

    const drawings = [...this.drawings];
    if (this.draftDrawing) {
      drawings.push(this.draftDrawing);
    }

    drawings.forEach(drawing => {
      const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      group.setAttribute('data-id', drawing.id);
      if (drawing.locked) {
        group.classList.add('is-locked');
      }
      if (drawing.id === this.selectedDrawingId) {
        group.classList.add('is-active');
      }
      this.renderDrawing(group, drawing);
      svg.appendChild(group);
    });
  }

  private renderDrawing(group: SVGGElement, drawing: DrawingObject): void {
    switch (drawing.type) {
      case 'vertical-line':
        this.renderVerticalLine(group, drawing);
        break;
      case 'horizontal-line':
        this.renderHorizontalLine(group, drawing);
        break;
      case 'trendline':
        this.renderTrendline(group, drawing);
        break;
      case 'channel':
        this.renderChannel(group, drawing);
        break;
      case 'fibonacci':
        this.renderFibonacci(group, drawing);
        break;
      case 'text':
        this.renderText(group, drawing);
        break;
      case 'rectangle':
        this.renderRectangle(group, drawing);
        break;
      case 'ellipse':
        this.renderEllipse(group, drawing);
        break;
      default:
        break;
    }
  }

  private renderVerticalLine(group: SVGGElement, drawing: DrawingObject): void {
    const point = drawing.points[0];
    const x = this.timeToCoordinate(point.time);
    if (x === null) {
      return;
    }
    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', `${x}`);
    line.setAttribute('x2', `${x}`);
    line.setAttribute('y1', '0');
    line.setAttribute('y2', `${this.chartContainer.nativeElement.clientHeight}`);
    line.setAttribute('stroke', drawing.style.strokeColor);
    line.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
    line.setAttribute('stroke-dasharray', '5,4');
    group.appendChild(line);
  }

  private renderHorizontalLine(group: SVGGElement, drawing: DrawingObject): void {
    const point = drawing.points[0];
    const y = this.priceToCoordinate(point.price);
    if (y === null) {
      return;
    }
    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', '0');
    line.setAttribute('x2', `${this.chartContainer.nativeElement.clientWidth}`);
    line.setAttribute('y1', `${y}`);
    line.setAttribute('y2', `${y}`);
    line.setAttribute('stroke', drawing.style.strokeColor);
    line.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
    group.appendChild(line);
  }

  private renderTrendline(group: SVGGElement, drawing: DrawingObject): void {
    if (drawing.points.length < 2) {
      return;
    }
    const from = drawing.points[0];
    const to = drawing.points[1];
    const x1 = this.timeToCoordinate(from.time);
    const x2 = this.timeToCoordinate(to.time);
    const y1 = this.priceToCoordinate(from.price);
    const y2 = this.priceToCoordinate(to.price);
    if (x1 === null || x2 === null || y1 === null || y2 === null) {
      return;
    }
    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', `${x1}`);
    line.setAttribute('y1', `${y1}`);
    line.setAttribute('x2', `${x2}`);
    line.setAttribute('y2', `${y2}`);
    line.setAttribute('stroke', drawing.style.strokeColor);
    line.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
    group.appendChild(line);
  }

  private renderChannel(group: SVGGElement, drawing: DrawingObject): void {
    if (drawing.points.length < 2) {
      return;
    }
    const width = drawing.points[2]?.price ?? 2;
    const base = { from: drawing.points[0], to: drawing.points[1] };
    const x1 = this.timeToCoordinate(base.from.time);
    const x2 = this.timeToCoordinate(base.to.time);
    const y1 = this.priceToCoordinate(base.from.price);
    const y2 = this.priceToCoordinate(base.to.price);
    if (x1 === null || x2 === null || y1 === null || y2 === null) {
      return;
    }
    const slope = (y2 - y1) / (x2 - x1 || 1);
    const offset = width * 10;
    const line1 = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line1.setAttribute('x1', `${x1}`);
    line1.setAttribute('y1', `${y1}`);
    line1.setAttribute('x2', `${x2}`);
    line1.setAttribute('y2', `${y2}`);
    line1.setAttribute('stroke', drawing.style.strokeColor);
    line1.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);

    const line2 = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line2.setAttribute('x1', `${x1}`);
    line2.setAttribute('y1', `${y1 + offset}`);
    line2.setAttribute('x2', `${x2}`);
    line2.setAttribute('y2', `${y2 + offset}`);
    line2.setAttribute('stroke', drawing.style.strokeColor);
    line2.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
    line2.setAttribute('stroke-dasharray', '6,4');

    const polygon = document.createElementNS('http://www.w3.org/2000/svg', 'polygon');
    polygon.setAttribute('points', `${x1},${y1} ${x2},${y2} ${x2},${y2 + offset} ${x1},${y1 + offset}`);
    polygon.setAttribute('fill', drawing.style.fillColor || 'rgba(56, 189, 248, 0.1)');
    polygon.setAttribute('stroke', 'none');

    group.appendChild(polygon);
    group.appendChild(line1);
    group.appendChild(line2);
  }

  private renderFibonacci(group: SVGGElement, drawing: DrawingObject): void {
    if (drawing.points.length < 2) {
      return;
    }
    const ratios = [0, 0.236, 0.382, 0.5, 0.618, 0.786, 1];
    const start = drawing.points[0];
    const end = drawing.points[1];
    const y1 = this.priceToCoordinate(start.price);
    const y2 = this.priceToCoordinate(end.price);
    const x1 = this.timeToCoordinate(start.time);
    const x2 = this.timeToCoordinate(end.time);
    if (x1 === null || x2 === null || y1 === null || y2 === null) {
      return;
    }
    const height = y2 - y1;
    ratios.forEach(ratio => {
      const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
      const y = y1 + height * ratio;
      line.setAttribute('x1', `${Math.min(x1, x2)}`);
      line.setAttribute('x2', `${Math.max(x1, x2)}`);
      line.setAttribute('y1', `${y}`);
      line.setAttribute('y2', `${y}`);
      line.setAttribute('stroke', drawing.style.strokeColor);
      line.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
      line.setAttribute('opacity', `${1 - ratio * 0.4}`);

      const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      text.setAttribute('x', `${Math.max(x1, x2) + 6}`);
      text.setAttribute('y', `${y + 4}`);
      text.setAttribute('fill', drawing.style.strokeColor);
      text.setAttribute('font-size', `${drawing.style.fontSize || 10}`);
      text.textContent = `${Math.round(ratio * 100)}%`;

      group.appendChild(line);
      group.appendChild(text);
    });
  }

  private renderText(group: SVGGElement, drawing: DrawingObject): void {
    const point = drawing.points[0];
    const x = this.timeToCoordinate(point.time);
    const y = this.priceToCoordinate(point.price);
    if (x === null || y === null) {
      return;
    }
    const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    text.setAttribute('x', `${x + 8}`);
    text.setAttribute('y', `${y - 8}`);
    text.setAttribute('fill', drawing.style.strokeColor);
    text.setAttribute('font-size', `${drawing.style.fontSize || 12}`);
    text.setAttribute('font-family', drawing.style.fontFamily || 'Inter, sans-serif');
    text.textContent = drawing.label || 'Note';
    group.appendChild(text);
  }

  private renderRectangle(group: SVGGElement, drawing: DrawingObject): void {
    if (drawing.points.length < 2) {
      return;
    }
    const start = drawing.points[0];
    const end = drawing.points[1];
    const x1 = this.timeToCoordinate(start.time);
    const y1 = this.priceToCoordinate(start.price);
    const x2 = this.timeToCoordinate(end.time);
    const y2 = this.priceToCoordinate(end.price);
    if (x1 === null || x2 === null || y1 === null || y2 === null) {
      return;
    }
    const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    rect.setAttribute('x', `${Math.min(x1, x2)}`);
    rect.setAttribute('y', `${Math.min(y1, y2)}`);
    rect.setAttribute('width', `${Math.abs(x2 - x1)}`);
    rect.setAttribute('height', `${Math.abs(y2 - y1)}`);
    rect.setAttribute('stroke', drawing.style.strokeColor);
    rect.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
    rect.setAttribute('fill', drawing.style.fillColor || 'rgba(14, 165, 233, 0.1)');
    group.appendChild(rect);
  }

  private renderEllipse(group: SVGGElement, drawing: DrawingObject): void {
    if (drawing.points.length < 2) {
      return;
    }
    const start = drawing.points[0];
    const end = drawing.points[1];
    const x1 = this.timeToCoordinate(start.time);
    const y1 = this.priceToCoordinate(start.price);
    const x2 = this.timeToCoordinate(end.time);
    const y2 = this.priceToCoordinate(end.price);
    if (x1 === null || x2 === null || y1 === null || y2 === null) {
      return;
    }
    const ellipse = document.createElementNS('http://www.w3.org/2000/svg', 'ellipse');
    ellipse.setAttribute('cx', `${(x1 + x2) / 2}`);
    ellipse.setAttribute('cy', `${(y1 + y2) / 2}`);
    ellipse.setAttribute('rx', `${Math.abs(x2 - x1) / 2}`);
    ellipse.setAttribute('ry', `${Math.abs(y2 - y1) / 2}`);
    ellipse.setAttribute('stroke', drawing.style.strokeColor);
    ellipse.setAttribute('stroke-width', `${drawing.style.strokeWidth}`);
    ellipse.setAttribute('fill', drawing.style.fillColor || 'rgba(34, 197, 94, 0.1)');
    group.appendChild(ellipse);
  }

  private toDrawingPoint(event: PointerEvent): DrawingPoint | null {
    const rect = this.chartContainer.nativeElement.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const time = this.chart.timeScale().coordinateToTime(x);
    let price = this.getActiveSeries().coordinateToPrice(y);
    if (time === null || time === undefined || price === null) {
      return null;
    }
    const timestamp = this.timeToTimestamp(time);

    if (this.magnetEnabled && this.seriesPayload) {
      const nearest = this.findNearestCandle(timestamp);
      if (nearest) {
        price = this.snapPrice(price, nearest);
      }
    }

    return { time: timestamp, price };
  }

  private timeToTimestamp(input: any): number {
    if (typeof input === 'number') {
      return input;
    }
    if (typeof input === 'object' && 'year' in input) {
      const date = new Date(Date.UTC(input.year, input.month - 1, input.day));
      return Math.floor(date.getTime() / 1000);
    }
    return Math.floor(Date.now() / 1000);
  }

  private priceToCoordinate(price: number): number | null {
    const series = this.getActiveSeries();
    if (!series) return null;
    return typeof series.priceToCoordinate === 'function' ? series.priceToCoordinate(price) : null;
  }

  private timeToCoordinate(time: number): number | null {
    return this.chart.timeScale().timeToCoordinate(time);
  }

  private getActiveSeries(): any {
    return this.displayMode === 'candles' ? this.candleSeries : this.lineSeries;
  }

  private findNearestCandle(time: number): ChartSeriesPayload['candles'][number] | null {
    if (!this.seriesPayload) {
      return null;
    }
    let nearest: ChartSeriesPayload['candles'][number] | null = null;
    let minDistance = Number.MAX_VALUE;
    for (const candle of this.seriesPayload.candles) {
      const distance = Math.abs(candle.time - time);
      if (distance < minDistance) {
        minDistance = distance;
        nearest = candle;
      }
    }
    return nearest;
  }

  private snapPrice(price: number, candle: ChartSeriesPayload['candles'][number]): number {
    const candidates = [candle.open, candle.high, candle.low, candle.close];
    let best = candidates[0];
    let minDiff = Math.abs(price - best);
    for (const candidate of candidates) {
      const diff = Math.abs(price - candidate);
      if (diff < minDiff) {
        minDiff = diff;
        best = candidate;
      }
    }
    return best;
  }

  private normalizeDrawing(drawing: DrawingObject): DrawingObject {
    const normalized = { ...drawing, points: [...drawing.points] };
    switch (drawing.type) {
      case 'vertical-line':
        normalized.points = [drawing.points[0]];
        break;
      case 'horizontal-line':
        normalized.points = [drawing.points[0]];
        break;
      case 'trendline':
      case 'rectangle':
      case 'ellipse':
      case 'fibonacci':
        normalized.points = drawing.points.slice(0, 2);
        break;
      case 'channel':
        if (drawing.points.length < 3) {
          const widthPoint: DrawingPoint = {
            time: drawing.points[1].time,
            price: drawing.points[1].price + 2
          };
          normalized.points = [...drawing.points.slice(0, 2), widthPoint];
        }
        break;
      case 'text':
        normalized.points = [drawing.points[0]];
        break;
      default:
        break;
    }
    return normalized;
  }

  private formatDate(timestamp?: number): string {
    if (!timestamp) {
      return '';
    }
    const date = new Date(timestamp * 1000);
    return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1)
      .toString()
      .padStart(2, '0')}/${date.getFullYear()}`;
  }
}
