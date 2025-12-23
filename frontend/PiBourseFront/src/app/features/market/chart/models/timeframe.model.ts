export type Timeframe =
  | 'M1'
  | 'M5'
  | 'M15'
  | 'M30'
  | 'H1'
  | 'H4'
  | 'D1'
  | 'W1'
  | 'MN';

export const TIMEFRAME_ORDER: Timeframe[] = ['M1', 'M5', 'M15', 'M30', 'H1', 'H4', 'D1', 'W1', 'MN'];

export function getTimeframeDuration(timeframe: Timeframe): number {
  switch (timeframe) {
    case 'M1':
      return 60 * 1000;
    case 'M5':
      return 5 * 60 * 1000;
    case 'M15':
      return 15 * 60 * 1000;
    case 'M30':
      return 30 * 60 * 1000;
    case 'H1':
      return 60 * 60 * 1000;
    case 'H4':
      return 4 * 60 * 60 * 1000;
    case 'D1':
      return 24 * 60 * 60 * 1000;
    case 'W1':
      return 7 * 24 * 60 * 60 * 1000;
    case 'MN':
      return 30 * 24 * 60 * 60 * 1000;
  }
}

export function getTimeframeDisplayLabel(timeframe: Timeframe): string {
  return timeframe;
}
