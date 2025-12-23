import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface DepthLevel {
  price: number;
  count: number;
  volume: number;
  cumulativeVolume: number;
}

@Component({
  selector: 'app-depth-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './depth-chart.component.html',
  styleUrls: ['./depth-chart.component.scss']
})
export class DepthChartComponent {
  @Input() buyLevels: DepthLevel[] = [];
  @Input() sellLevels: DepthLevel[] = [];
  @Input() width = 720;
  @Input() height = 400;

  // Header (stats)
  @Input() symbol = 'BTCUSD';
  @Input() bestBid: number | null = null;
  @Input() bestAsk: number | null = null;
  @Input() lastPrice: number | null = null;
  @Input() openPrice: number | null = null;
  @Input() highPrice: number | null = null;
  @Input() lowPrice: number | null = null;
  @Input() volume: number = 0;

  // -------- Volume max (pour l’échelle Y) ------------------------------

  get maxVolume(): number {
    const b = this.buyLevels.length > 0 ? Math.max(...this.buyLevels.map(l => l.cumulativeVolume)) : 0;
    const s = this.sellLevels.length > 0 ? Math.max(...this.sellLevels.map(l => l.cumulativeVolume)) : 0;
    const m = Math.max(b, s);
    return m > 0 ? m : 1;
  }

  // Y = fonction du volume cumulé (0 → height - 60)
  y(v: number): number {
    const chartHeight = this.height - 60;
    return chartHeight - (v / this.maxVolume) * chartHeight;
  }

  // Largeur de chaque barre
  get barWidth(): number {
    const totalBars = this.buyLevels.length + this.sellLevels.length;
    return totalBars > 0 ? Math.max(8, (this.width - 80) / totalBars) : 10;
  }

  // Générer les barres pour buy
  get buyBars(): Array<{x: number, y: number, height: number, price: number, volume: number}> {
    if (!this.buyLevels || this.buyLevels.length === 0) return [];
    
    const bids = [...this.buyLevels].sort((a, b) => b.price - a.price);
    const bars: Array<{x: number, y: number, height: number, price: number, volume: number}> = [];
    const chartHeight = this.height - 60;
    const barWidth = this.barWidth;
    
    bids.forEach((level, i) => {
      const x = 40 + i * barWidth;
      const barHeight = (level.volume / this.maxVolume) * chartHeight;
      const y = chartHeight - barHeight;
      bars.push({ x, y, height: barHeight, price: level.price, volume: level.volume });
    });
    
    return bars;
  }

  // Générer les barres pour sell
  get sellBars(): Array<{x: number, y: number, height: number, price: number, volume: number}> {
    if (!this.sellLevels || this.sellLevels.length === 0) return [];
    
    const asks = [...this.sellLevels].sort((a, b) => a.price - b.price);
    const bars: Array<{x: number, y: number, height: number, price: number, volume: number}> = [];
    const chartHeight = this.height - 60;
    const barWidth = this.barWidth;
    const startX = 40 + this.buyLevels.length * barWidth;
    
    asks.forEach((level, i) => {
      const x = startX + i * barWidth;
      const barHeight = (level.volume / this.maxVolume) * chartHeight;
      const y = chartHeight - barHeight;
      bars.push({ x, y, height: barHeight, price: level.price, volume: level.volume });
    });
    
    return bars;
  }

  // X côté BUY = moitié gauche (centre -> bord gauche) avec espace
  xBuy(i: number, total: number): number {
    if (total <= 1) {
      return this.width / 2 - 10;
    }
    const ratio = i / (total - 1); // 0 ... 1
    return (this.width / 2 - 10) - ratio * ((this.width / 2) - 40); // 30px de marge à gauche
  }

  // X côté SELL = moitié droite (centre -> bord droit) avec espace
  xSell(i: number, total: number): number {
    if (total <= 1) {
      return this.width / 2 + 10;
    }
    const ratio = i / (total - 1); // 0 ... 1
    return (this.width / 2 + 10) + ratio * ((this.width / 2) - 40); // 30px de marge à droite
  }

  // -------- Labels de prix pour chaque côté ---------------------------

  get buyPriceLabels(): Array<{ x: number; price: number }> {
    if (!this.buyLevels || this.buyLevels.length === 0) {
      return [];
    }

    // On trie les bids du plus cher (près du centre) au moins cher (à gauche)
    const bids = [...this.buyLevels].sort((a, b) => b.price - a.price);
    const labels: Array<{ x: number; price: number }> = [];
    
    if (bids.length === 1) {
      labels.push({
        x: this.xBuy(0, bids.length),
        price: bids[0].price
      });
      return labels;
    }
    
    // Toujours afficher le premier (plus cher, proche du centre)
    labels.push({
      x: this.xBuy(0, bids.length),
      price: bids[0].price
    });
    
    // Afficher 3 labels intermédiaires
    const numIntermediate = 3;
    for (let i = 1; i <= numIntermediate; i++) {
      const idx = Math.floor((bids.length - 1) * i / (numIntermediate + 1));
      if (idx > 0 && idx < bids.length - 1) {
        labels.push({
          x: this.xBuy(idx, bids.length),
          price: bids[idx].price
        });
      }
    }
    
    // Toujours afficher le dernier (moins cher, bord gauche)
    labels.push({
      x: this.xBuy(bids.length - 1, bids.length),
      price: bids[bids.length - 1].price
    });
    
    return labels;
  }

  get sellPriceLabels(): Array<{ x: number; price: number }> {
    if (!this.sellLevels || this.sellLevels.length === 0) {
      return [];
    }

    // Asks du moins cher (près du centre) au plus cher (à droite)
    const asks = [...this.sellLevels].sort((a, b) => a.price - b.price);
    const labels: Array<{ x: number; price: number }> = [];
    
    if (asks.length === 1) {
      labels.push({
        x: this.xSell(0, asks.length),
        price: asks[0].price
      });
      return labels;
    }
    
    // Toujours afficher le premier (moins cher, proche du centre)
    labels.push({
      x: this.xSell(0, asks.length),
      price: asks[0].price
    });
    
    // Afficher 3 labels intermédiaires
    const numIntermediate = 3;
    for (let i = 1; i <= numIntermediate; i++) {
      const idx = Math.floor((asks.length - 1) * i / (numIntermediate + 1));
      if (idx > 0 && idx < asks.length - 1) {
        labels.push({
          x: this.xSell(idx, asks.length),
          price: asks[idx].price
        });
      }
    }
    
    // Toujours afficher le dernier (plus cher, bord droit)
    labels.push({
      x: this.xSell(asks.length - 1, asks.length),
      price: asks[asks.length - 1].price
    });
    
    return labels;
  }

  // -------- Ticks Volume (axe Y) --------------------------------------

  get volumeTicksY(): Array<{ y: number; label: string }> {
    const ticks: Array<{ y: number; label: string }> = [];
    const numTicks = 4;
    for (let i = 0; i <= numTicks; i++) {
      const volume = (this.maxVolume * i) / numTicks;
      const yPos = this.y(volume);
      ticks.push({ y: yPos, label: volume.toFixed(0) });
    }
    return ticks;
  }

  // -------- Paths SVG BUY / SELL --------------------------------------

  get buyPath(): string | null {
    if (!this.buyLevels || this.buyLevels.length === 0) {
      return null;
    }

    const chartHeight = this.height - 60;

    // Bids triés du plus cher au moins cher
    const bids = [...this.buyLevels].sort((a, b) => b.price - a.price);
    const path: string[] = [];

    // départ au centre en bas (vallée) avec offset
    path.push(`M ${this.width / 2 - 10} ${chartHeight}`);

    // on va vers la gauche (prix décroissants)
    for (let i = 0; i < bids.length; i++) {
      const x = this.xBuy(i, bids.length);
      const yv = this.y(bids[i].cumulativeVolume);
      path.push(`L ${x} ${yv}`);
    }

    // retour en bas sur le bord gauche
    const endX = this.xBuy(bids.length - 1, bids.length);
    path.push(`L ${endX} ${chartHeight} Z`);

    return path.join(' ');
  }

  get sellPath(): string | null {
    if (!this.sellLevels || this.sellLevels.length === 0) {
      return null;
    }

    const chartHeight = this.height - 60;

    // Asks triés du moins cher au plus cher
    const asks = [...this.sellLevels].sort((a, b) => a.price - b.price);
    const path: string[] = [];

    // départ au centre en bas (vallée) avec offset
    path.push(`M ${this.width / 2 + 10} ${chartHeight}`);

    // on va vers la droite (prix croissants)
    for (let i = 0; i < asks.length; i++) {
      const x = this.xSell(i, asks.length);
      const yv = this.y(asks[i].cumulativeVolume);
      path.push(`L ${x} ${yv}`);
    }

    // retour en bas sur le bord droit
    const endX = this.xSell(asks.length - 1, asks.length);
    path.push(`L ${endX} ${chartHeight} Z`);

    return path.join(' ');
  }
}
