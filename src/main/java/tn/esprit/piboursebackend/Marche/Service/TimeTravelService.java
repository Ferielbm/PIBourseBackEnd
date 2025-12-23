package tn.esprit.piboursebackend.Marche.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Marche.Entity.*;
import tn.esprit.piboursebackend.Marche.Repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeTravelService {

    private final TimeTravelSessionRepository sessionRepository;
    private final AlternativeTradeRepository tradeRepository;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final MarketDataService marketDataService;

    private static final int MAX_ACTIVE_SESSIONS = 3;

    @Transactional
    public TimeTravelSession startTimeTravelSession(String playerId, LocalDateTime rewindToDate) {
        // Validation
        validateTimeTravelRequest(playerId, rewindToDate);

        // Créer la session
        TimeTravelSession session = TimeTravelSession.builder()
                .sessionId(generateSessionId())
                .playerId(playerId)
                .rewindToDate(rewindToDate)
                .currentSimulationDate(rewindToDate)
                .originalTimelineDate(LocalDateTime.now())
                .status(TimeTravelSession.TimeTravelStatus.ACTIVE)
                .originalPortfolioValue(calculateCurrentPortfolioValue(playerId))
                .alternativePortfolioValue(calculateCurrentPortfolioValue(playerId))
                .build();

        TimeTravelSession savedSession = sessionRepository.save(session);
        log.info("🚀 Time travel session started: {} for player {}", savedSession.getSessionId(), playerId);

        return savedSession;
    }

    @Transactional
    public AlternativeTrade executeAlternativeTrade(String sessionId, AlternativeTrade tradeRequest) {
        TimeTravelSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Valider que la date d'exécution est après la date de rewind
        if (tradeRequest.getExecutionDate().isBefore(session.getRewindToDate())) {
            throw new RuntimeException("Trade execution date cannot be before rewind date");
        }

        // Vérifier le prix historique
        BigDecimal historicalPrice = getHistoricalPrice(tradeRequest.getSymbol(), tradeRequest.getExecutionDate());
        tradeRequest.setExecutionPrice(historicalPrice);

        // Créer le trade alternatif
        AlternativeTrade trade = AlternativeTrade.builder()
                .symbol(tradeRequest.getSymbol())
                .action(tradeRequest.getAction())
                .quantity(tradeRequest.getQuantity())
                .executionPrice(historicalPrice)
                .executionDate(tradeRequest.getExecutionDate())
                .originalDecision("NOT_EXECUTED") // À remplacer par les données réelles
                .alternativeDecision(tradeRequest.getAlternativeDecision())
                .session(session)
                .build();

        AlternativeTrade savedTrade = tradeRepository.save(trade);

        // Mettre à jour la valeur du portfolio alternatif
        updateAlternativePortfolioValue(session);

        log.info("📊 Alternative trade executed: {} {} shares of {} at ${}",
                tradeRequest.getAction(), tradeRequest.getQuantity(),
                tradeRequest.getSymbol(), historicalPrice);

        return savedTrade;
    }

    @Transactional
    public TimeTravelResult completeTimeTravelSession(String sessionId) {
        TimeTravelSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Calculer les performances
        TimeTravelResult result = calculateTimeTravelResults(session);

        // Marquer la session comme terminée
        session.setStatus(TimeTravelSession.TimeTravelStatus.COMPLETED);
        sessionRepository.save(session);

        log.info("🎯 Time travel session completed: {}", sessionId);
        return result;
    }

    public List<TimeTravelSession> getPlayerSessions(String playerId) {
        return sessionRepository.findByPlayerIdOrderByCreatedAtDesc(playerId);
    }

    public TimeTravelResult previewResults(String sessionId) {
        TimeTravelSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        return calculateTimeTravelResults(session);
    }

    // === MÉTHODES PRIVÉES ===

    private void validateTimeTravelRequest(String playerId, LocalDateTime rewindToDate) {
        // Vérifier le nombre de sessions actives
        long activeSessions = sessionRepository.countActiveSessionsByPlayer(playerId);
        if (activeSessions >= MAX_ACTIVE_SESSIONS) {
            throw new RuntimeException("Maximum active time travel sessions reached: " + MAX_ACTIVE_SESSIONS);
        }

        // Vérifier que la date de rewind est dans le passé
        if (rewindToDate.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Cannot rewind to future date");
        }

        // Vérifier que la date a des données historiques (au moins 2023)
        if (rewindToDate.isBefore(LocalDateTime.of(2023, 1, 1, 0, 0))) {
            throw new RuntimeException("No historical data available before 2023");
        }
    }

    private String generateSessionId() {
        return "TT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private BigDecimal calculateCurrentPortfolioValue(String playerId) {
        // Simulation - À remplacer par le vrai service de portfolio
        // Pour l'instant, retourne une valeur fixe
        return new BigDecimal("100000.00");
    }

    private BigDecimal getHistoricalPrice(String symbol, LocalDateTime date) {
        // Récupérer le prix historique le plus proche
        List<PriceHistory> priceHistory = priceHistoryRepository
                .findByStock_SymbolAndDateTimeBeforeOrderByDateTimeDesc(symbol, date,
                        org.springframework.data.domain.PageRequest.of(0, 1));

        if (priceHistory.isEmpty()) {
            throw new RuntimeException("No historical price found for " + symbol + " at " + date);
        }

        return priceHistory.get(0).getClosePrice();
    }

    private void updateAlternativePortfolioValue(TimeTravelSession session) {
        // Simulation de calcul de valeur de portfolio
        // À intégrer avec le vrai système de trading
        BigDecimal baseValue = session.getAlternativePortfolioValue();
        BigDecimal randomChange = BigDecimal.valueOf((Math.random() - 0.5) * 0.1).multiply(baseValue);
        session.setAlternativePortfolioValue(baseValue.add(randomChange));
        sessionRepository.save(session);
    }

    private TimeTravelResult calculateTimeTravelResults(TimeTravelSession session) {
        List<AlternativeTrade> trades = tradeRepository.findBySession_SessionId(session.getSessionId());

        // Calcul des performances
        BigDecimal originalPerformance = calculatePerformance(session.getOriginalPortfolioValue(),
                new BigDecimal("110000.00")); // Simulation
        BigDecimal alternativePerformance = calculatePerformance(session.getOriginalPortfolioValue(),
                session.getAlternativePortfolioValue());

        BigDecimal performanceGap = alternativePerformance.subtract(originalPerformance);

        // Générer les insights d'apprentissage
        List<TimeTravelResult.LearningInsight> insights = generateLearningInsights(trades, performanceGap);

        // Comparaison des trades
        List<TimeTravelResult.TradeComparison> tradeComparisons = generateTradeComparisons(trades);

        return TimeTravelResult.builder()
                .sessionId(session.getSessionId())
                .playerId(session.getPlayerId())
                .rewindPoint(session.getRewindToDate())
                .simulationEndDate(LocalDateTime.now())
                .originalPerformance(originalPerformance)
                .alternativePerformance(alternativePerformance)
                .performanceGap(performanceGap)
                .tradeComparisons(tradeComparisons)
                .learningInsights(insights)
                .riskAssessment(assessRisk(trades))
                .originalSharpeRatio(calculateSharpeRatio(originalPerformance, new BigDecimal("0.15")))
                .alternativeSharpeRatio(calculateSharpeRatio(alternativePerformance, new BigDecimal("0.12")))
                .maxDrawdownImprovement(calculateMaxDrawdownImprovement())
                .build();
    }

    private BigDecimal calculatePerformance(BigDecimal startValue, BigDecimal endValue) {
        return endValue.subtract(startValue)
                .divide(startValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private List<TimeTravelResult.LearningInsight> generateLearningInsights(List<AlternativeTrade> trades, BigDecimal performanceGap) {
        List<TimeTravelResult.LearningInsight> insights = new ArrayList<>();

        if (performanceGap.compareTo(BigDecimal.ZERO) > 0) {
            insights.add(TimeTravelResult.LearningInsight.builder()
                    .insightType("OPPORTUNITY_IDENTIFIED")
                    .description("You identified key opportunities by changing " + trades.size() + " decisions")
                    .impactScore(performanceGap)
                    .recommendation("Focus on sectors where your alternative decisions performed best")
                    .build());
        }

        // Ajouter plus d'insights basés sur l'analyse des trades
        if (trades.size() > 5) {
            insights.add(TimeTravelResult.LearningInsight.builder()
                    .insightType("OVERTRADING_INSIGHT")
                    .description("You made " + trades.size() + " alternative trades - consider quality over quantity")
                    .impactScore(new BigDecimal("-0.05"))
                    .recommendation("Focus on high-conviction trades rather than frequent trading")
                    .build());
        }

        return insights;
    }

    private List<TimeTravelResult.TradeComparison> generateTradeComparisons(List<AlternativeTrade> trades) {
        return trades.stream()
                .map(trade -> TimeTravelResult.TradeComparison.builder()
                        .symbol(trade.getSymbol())
                        .originalAction("HOLD") // À remplacer par données réelles
                        .alternativeAction(trade.getAction().toString())
                        .originalPnL(calculatePnL(trade, false))
                        .alternativePnL(calculatePnL(trade, true))
                        .improvement(calculateImprovement(trade))
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculatePnL(AlternativeTrade trade, boolean isAlternative) {
        // Simulation de calcul PnL
        return BigDecimal.valueOf(Math.random() * 1000).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateImprovement(AlternativeTrade trade) {
        return BigDecimal.valueOf(Math.random() * 500).setScale(2, RoundingMode.HALF_UP);
    }

    private String assessRisk(List<AlternativeTrade> trades) {
        long riskyTrades = trades.stream()
                .filter(trade -> trade.getAction() == AlternativeTrade.TradeAction.BUY)
                .count();

        double riskRatio = (double) riskyTrades / trades.size();
        return riskRatio > 0.7 ? "HIGH" : riskRatio > 0.4 ? "MEDIUM" : "LOW";
    }

    private BigDecimal calculateSharpeRatio(BigDecimal performance, BigDecimal volatility) {
        return performance.divide(volatility, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMaxDrawdownImprovement() {
        return BigDecimal.valueOf(Math.random() * 0.1).setScale(4, RoundingMode.HALF_UP);
    }
}