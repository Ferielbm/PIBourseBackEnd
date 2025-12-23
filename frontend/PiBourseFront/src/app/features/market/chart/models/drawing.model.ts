export type DrawingTool =
  | 'cursor'
  | 'crosshair'
  | 'vertical-line'
  | 'horizontal-line'
  | 'trendline'
  | 'channel'
  | 'fibonacci'
  | 'text'
  | 'rectangle'
  | 'ellipse';

export interface DrawingStyle {
  strokeColor: string;
  strokeWidth: number;
  fillColor?: string;
  opacity?: number;
  fontSize?: number;
  fontFamily?: string;
}

export interface DrawingPoint {
  time: number;
  price: number;
}

export interface DrawingObject {
  id: string;
  type: DrawingTool;
  points: DrawingPoint[];
  locked: boolean;
  style: DrawingStyle;
  label?: string;
}
