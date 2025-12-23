import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarketChartComponent } from './market-chart.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MarketChartComponent
  ],
  exports: [MarketChartComponent]
})
export class ChartModule {}
