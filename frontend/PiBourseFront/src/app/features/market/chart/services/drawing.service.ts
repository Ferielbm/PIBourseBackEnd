import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { DrawingObject, DrawingStyle, DrawingTool } from '../models/drawing.model';

@Injectable({ providedIn: 'root' })
export class DrawingService {
  private readonly drawingsSubject = new BehaviorSubject<DrawingObject[]>([]);
  readonly drawings$ = this.drawingsSubject.asObservable();

  getAll(): DrawingObject[] {
    return this.drawingsSubject.value;
  }

  add(drawing: DrawingObject): void {
    this.drawingsSubject.next([...this.drawingsSubject.value, drawing]);
  }

  update(id: string, patch: Partial<DrawingObject>): void {
    this.drawingsSubject.next(
      this.drawingsSubject.value.map(d => (d.id === id ? { ...d, ...patch } : d))
    );
  }

  remove(id: string): void {
    this.drawingsSubject.next(this.drawingsSubject.value.filter(d => d.id !== id));
  }

  setLocked(id: string, locked: boolean): void {
    this.update(id, { locked });
  }

  clear(): void {
    this.drawingsSubject.next([]);
  }

  getDefaultStyle(tool: DrawingTool): DrawingStyle {
    switch (tool) {
      case 'vertical-line':
      case 'horizontal-line':
        return { strokeColor: '#38bdf8', strokeWidth: 1.5 };
      case 'trendline':
        return { strokeColor: '#facc15', strokeWidth: 2 };
      case 'channel':
        return { strokeColor: '#34d399', strokeWidth: 1.5, fillColor: 'rgba(34, 211, 238, 0.12)' };
      case 'fibonacci':
        return { strokeColor: '#f472b6', strokeWidth: 1, fontSize: 11 };
      case 'text':
        return { strokeColor: '#e2e8f0', strokeWidth: 1, fontSize: 13, fontFamily: 'Inter, sans-serif' };
      case 'rectangle':
        return { strokeColor: '#60a5fa', strokeWidth: 1.5, fillColor: 'rgba(59, 130, 246, 0.12)' };
      case 'ellipse':
        return { strokeColor: '#f97316', strokeWidth: 1.5, fillColor: 'rgba(249, 115, 22, 0.1)' };
      default:
        return { strokeColor: '#38bdf8', strokeWidth: 1.5 };
    }
  }
}
