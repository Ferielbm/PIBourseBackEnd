package tn.esprit.piboursebackend.Marche.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Repository.PriceHistoryRepository;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MarketStatisticsService {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public Map<String, Object> getStockPerformance(String symbol) {
        List<PriceHistory> history = priceHistoryRepository.findByStock_SymbolOrderByDateTime(symbol);

        if (history.isEmpty()) {
            return Map.of("error", "No data found for symbol: " + symbol);
        }

        PriceHistory firstDay = history.get(0);
        PriceHistory lastDay = history.get(history.size() - 1);

        BigDecimal totalReturn = lastDay.getClosePrice()
                .subtract(firstDay.getClosePrice())
                .divide(firstDay.getClosePrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        BigDecimal volatility = calculateVolatility(history);
        Map<String, BigDecimal> monthlyPerformance = calculateMonthlyPerformance(history);
        BigDecimal averageVolume = calculateAverageVolume(history);

        return Map.of(
                "symbol", symbol,
                "totalReturn", totalReturn.setScale(2, RoundingMode.HALF_UP) + "%",
                "volatility", volatility + "%",
                "startPrice", firstDay.getClosePrice(),
                "endPrice", lastDay.getClosePrice(),
                "priceChange", lastDay.getClosePrice().subtract(firstDay.getClosePrice()),
                "totalDays", history.size(),
                "averageVolume", averageVolume,
                "monthlyPerformance", monthlyPerformance
        );
    }

    public List<Map<String, Object>> getTopPerformers(int limit) {
        List<Stock> allStocks = stockRepository.findAll();
        List<Map<String, Object>> performers = new ArrayList<>();

        for (Stock stock : allStocks) {
            Map<String, Object> performance = getStockPerformance(stock.getSymbol());
            if (!performance.containsKey("error")) {
                performers.add(performance);
            }
        }

        performers.sort((a, b) -> {
            String aReturn = ((String) a.get("totalReturn")).replace("%", "");
            String bReturn = ((String) b.get("totalReturn")).replace("%", "");
            return new BigDecimal(bReturn).compareTo(new BigDecimal(aReturn));
        });

        return performers.subList(0, Math.min(limit, performers.size()));
    }

    public Map<String, Object> getMarketOverview() {
        List<Stock> allStocks = stockRepository.findAll();
        Map<String, Object> overview = new HashMap<>();

        BigDecimal totalMarketCap = allStocks.stream()
                .map(Stock::getMarketCap)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> stocksBySector = new HashMap<>();
        Map<String, BigDecimal> marketCapBySector = new HashMap<>();

        for (Stock stock : allStocks) {
            String sector = stock.getSector();
            stocksBySector.merge(sector, 1L, Long::sum);
            marketCapBySector.merge(sector, stock.getMarketCap(), BigDecimal::add);
        }

        overview.put("totalStocks", allStocks.size());
        overview.put("totalMarketCap", totalMarketCap);
        overview.put("stocksBySector", stocksBySector);
        overview.put("marketCapBySector", marketCapBySector);
        overview.put("averageStockPrice", calculateAverageStockPrice(allStocks));
        overview.put("lastUpdate", LocalDateTime.now());

        return overview;
    }

    private BigDecimal calculateVolatility(List<PriceHistory> history) {
        if (history.size() < 2) return BigDecimal.ZERO;

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            BigDecimal dailyReturn = history.get(i).getClosePrice()
                    .subtract(history.get(i-1).getClosePrice())
                    .divide(history.get(i-1).getClosePrice(), 6, RoundingMode.HALF_UP);
            returns.add(dailyReturn);
        }

        BigDecimal mean = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(returns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
                .map(ret -> ret.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(returns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal stdDev = new BigDecimal(Math.sqrt(variance.doubleValue()));
        return stdDev.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> calculateMonthlyPerformance(List<PriceHistory> history) {
        Map<String, BigDecimal> monthlyPerf = new TreeMap<>();
        Map<String, List<PriceHistory>> byMonth = new HashMap<>();

        for (PriceHistory ph : history) {
            String monthKey = ph.getDateTime().getMonth().toString() + "-" + ph.getDateTime().getYear();
            byMonth.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(ph);
        }

        for (Map.Entry<String, List<PriceHistory>> entry : byMonth.entrySet()) {
            List<PriceHistory> monthData = entry.getValue();
            if (monthData.size() > 1) {
                PriceHistory first = monthData.get(0);
                PriceHistory last = monthData.get(monthData.size() - 1);

                BigDecimal monthlyReturn = last.getClosePrice()
                        .subtract(first.getClosePrice())
                        .divide(first.getClosePrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

                monthlyPerf.put(entry.getKey(), monthlyReturn.setScale(2, RoundingMode.HALF_UP));
            }
        }

        return monthlyPerf;
    }

    private BigDecimal calculateAverageVolume(List<PriceHistory> history) {
        return BigDecimal.valueOf(
                history.stream()
                        .mapToLong(PriceHistory::getVolume)
                        .average()
                        .orElse(0)
        ).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageStockPrice(List<Stock> stocks) {
        return BigDecimal.valueOf(
                stocks.stream()
                        .mapToDouble(stock -> stock.getCurrentPrice().doubleValue())
                        .average()
                        .orElse(0)
        ).setScale(2, RoundingMode.HALF_UP);
    }
}