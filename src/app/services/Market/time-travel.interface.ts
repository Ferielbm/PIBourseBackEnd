export interface TimeTravelResult {
  sessionId: string;
  originalPerformance: number;
  alternativePerformance: number;
  performanceGap: number;
  tradeComparisons: TradeComparison[];
  learningInsights: LearningInsight[];
  riskAssessment: 'LOW' | 'MEDIUM' | 'HIGH';
  alternativeSharpeRatio: number;
  maxDrawdownImprovement: number;
}

export interface TradeComparison {
  symbol: string;
  originalAction: string;
  alternativeAction: string;
  originalPnL: number;
  alternativePnL: number;
  improvement: number;
}

export interface LearningInsight {
  insightType: string;
  description: string;
  recommendation: string;
  impactScore: number;
}
