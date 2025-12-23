package tn.esprit.piboursebackend.Marche.Entity;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeTravelResult {
    private String sessionId;
    private String playerId;
    private LocalDateTime rewindPoint;
    private LocalDateTime simulationEndDate;

    // Performances
    private BigDecimal originalPerformance;
    private BigDecimal alternativePerformance;
    private BigDecimal performanceGap;

    // Analyses
    private List<TradeComparison> tradeComparisons;
    private Map<String, BigDecimal> sectorImpact;
    private List<LearningInsight> learningInsights;
    private String riskAssessment;

    // Métriques avancées
    private BigDecimal originalSharpeRatio;
    private BigDecimal alternativeSharpeRatio;
    private BigDecimal maxDrawdownImprovement;

    @Data
    @Builder
    public static class TradeComparison {
        private String symbol;
        private String originalAction;
        private String alternativeAction;
        private BigDecimal originalPnL;
        private BigDecimal alternativePnL;
        private BigDecimal improvement;
    }

    @Data
    @Builder
    public static class LearningInsight {
        private String insightType;
        private String description;
        private BigDecimal impactScore;
        private String recommendation;
    }
}