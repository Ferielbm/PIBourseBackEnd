import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Subscription } from 'rxjs';
import { OrderBookStats, OrderBookStatsService } from '../../../../services/Order/order-book-stats.service';

@Component({
  selector: 'app-stats-bar',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './stats-bar.component.html',
  styleUrls: ['./stats-bar.component.scss']
})
export class StatsBarComponent implements OnInit, OnDestroy {
  @Input() symbol!: string;
  @Input() pollInterval = 2000;

  stats: OrderBookStats | null = null;
  private sub?: Subscription;

  constructor(private statsService: OrderBookStatsService) {}

  ngOnInit(): void {
    if (this.symbol) {
      this.sub = this.statsService.pollStats(this.symbol, this.pollInterval)
        .subscribe(s => this.stats = s);
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
