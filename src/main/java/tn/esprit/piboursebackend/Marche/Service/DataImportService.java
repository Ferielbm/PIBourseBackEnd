package tn.esprit.piboursebackend.Marche.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Marche.Entity.*;
import tn.esprit.piboursebackend.Marche.Repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final MarketRepository marketRepository;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final OrderBookRepository orderBookRepository;

    @Transactional
    public String importFullYearData() {
        try {
            log.info("🚀 Starting full year market data import...");

            // 1. Nettoyage COMPLET dans le bon ordre (pour éviter les contraintes de clés étrangères)
            cleanAllData();

            // 2. Création des marchés
            Market nasdaq = createMarket("NASDAQ", "NASDAQ Stock Market");
            Market nyse = createMarket("NYSE", "New York Stock Exchange");

            log.info("✅ Markets created: NASDAQ, NYSE");

            // 3. Import des stocks avec données annuelles
            importStocksWithFullYearData();

            log.info("🎉 Full 2023 market data imported successfully!");
            return "✅ Full 2023 market data imported successfully! " +
                    "Markets: NASDAQ, NYSE | 50 stocks | 252 days of historical data";

        } catch (Exception e) {
            log.error("❌ Market data initialization failed", e);
            throw new RuntimeException("Market data initialization failed: " + e.getMessage(), e);
        }
    }

    private void cleanAllData() {
        log.info("🧹 Cleaning existing data...");

        // Ordre CRITIQUE : supprimer d'abord les enfants, puis les parents
        try {
            priceHistoryRepository.deleteAll();
            log.info("✅ Price history deleted");
        } catch (Exception e) {
            log.warn("⚠️ No price history to delete or error during deletion: {}", e.getMessage());
        }

        try {
            orderBookRepository.deleteAll();
            log.info("✅ Order books deleted");
        } catch (Exception e) {
            log.warn("⚠️ No order books to delete or error during deletion: {}", e.getMessage());
        }

        try {
            stockRepository.deleteAll();
            log.info("✅ Stocks deleted");
        } catch (Exception e) {
            log.warn("⚠️ No stocks to delete or error during deletion: {}", e.getMessage());
        }

        try {
            marketRepository.deleteAll();
            log.info("✅ Markets deleted");
        } catch (Exception e) {
            log.warn("⚠️ No markets to delete or error during deletion: {}", e.getMessage());
        }

        // Petite pause pour s'assurer que tout est nettoyé
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Market createMarket(String code, String name) {
        Market market = Market.builder()
                .code(code)
                .name(name)
                .currentDate(LocalDateTime.of(2023, 1, 1, 9, 30)) // Corrigé: currentMarketDate au lieu de currentDate
                .isOpen(true)
                .timeCompressionRatio(new BigDecimal("168.0"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return marketRepository.save(market);
    }

    private void importStocksWithFullYearData() {
        log.info("📊 Importing 50 stocks with full year 2023 data...");

        // 50 actions principales avec données réelles 2023
        List<StockData> stockDataList = List.of(
                // TECHNOLOGY (15 actions)
                new StockData("AAPL", "Apple Inc.", "Technology", new BigDecimal("2800000000000"), new BigDecimal("182.63")),
                new StockData("MSFT", "Microsoft Corporation", "Technology", new BigDecimal("2800000000000"), new BigDecimal("374.51")),
                new StockData("GOOGL", "Alphabet Inc.", "Technology", new BigDecimal("1750000000000"), new BigDecimal("138.57")),
                new StockData("AMZN", "Amazon.com Inc.", "Technology", new BigDecimal("1550000000000"), new BigDecimal("151.94")),
                new StockData("META", "Meta Platforms Inc.", "Technology", new BigDecimal("935000000000"), new BigDecimal("351.95")),
                new StockData("NVDA", "NVIDIA Corporation", "Technology", new BigDecimal("1100000000000"), new BigDecimal("477.76")),
                new StockData("TSLA", "Tesla Inc.", "Automotive", new BigDecimal("800000000000"), new BigDecimal("248.42")),
                new StockData("ADBE", "Adobe Inc.", "Technology", new BigDecimal("270000000000"), new BigDecimal("630.23")),
                new StockData("INTC", "Intel Corporation", "Technology", new BigDecimal("190000000000"), new BigDecimal("50.20")),
                new StockData("CSCO", "Cisco Systems Inc.", "Technology", new BigDecimal("200000000000"), new BigDecimal("55.30")),
                new StockData("ORCL", "Oracle Corporation", "Technology", new BigDecimal("320000000000"), new BigDecimal("120.45")),
                new StockData("IBM", "International Business Machines", "Technology", new BigDecimal("165000000000"), new BigDecimal("185.60")),
                new StockData("QCOM", "Qualcomm Inc.", "Technology", new BigDecimal("180000000000"), new BigDecimal("145.80")),
                new StockData("TXN", "Texas Instruments", "Technology", new BigDecimal("170000000000"), new BigDecimal("185.90")),
                new StockData("AVGO", "Broadcom Inc.", "Technology", new BigDecimal("580000000000"), new BigDecimal("1025.50")),

                // FINANCE (10 actions)
                new StockData("JPM", "JPMorgan Chase & Co.", "Financial Services", new BigDecimal("480000000000"), new BigDecimal("170.10")),
                new StockData("BAC", "Bank of America Corp.", "Financial Services", new BigDecimal("240000000000"), new BigDecimal("33.60")),
                new StockData("WFC", "Wells Fargo & Co.", "Financial Services", new BigDecimal("160000000000"), new BigDecimal("48.90")),
                new StockData("GS", "Goldman Sachs Group Inc.", "Financial Services", new BigDecimal("120000000000"), new BigDecimal("382.40")),
                new StockData("BLK", "BlackRock Inc.", "Financial Services", new BigDecimal("115000000000"), new BigDecimal("785.30")),
                new StockData("MS", "Morgan Stanley", "Financial Services", new BigDecimal("150000000000"), new BigDecimal("85.40")),
                new StockData("SCHW", "Charles Schwab Corp.", "Financial Services", new BigDecimal("130000000000"), new BigDecimal("68.25")),
                new StockData("AXP", "American Express Co.", "Financial Services", new BigDecimal("140000000000"), new BigDecimal("185.60")),
                new StockData("V", "Visa Inc.", "Financial Services", new BigDecimal("500000000000"), new BigDecimal("250.80")),
                new StockData("MA", "Mastercard Inc.", "Financial Services", new BigDecimal("380000000000"), new BigDecimal("410.30")),

                // HEALTHCARE (10 actions)
                new StockData("JNJ", "Johnson & Johnson", "Healthcare", new BigDecimal("380000000000"), new BigDecimal("155.50")),
                new StockData("PFE", "Pfizer Inc.", "Healthcare", new BigDecimal("200000000000"), new BigDecimal("40.50")),
                new StockData("MRK", "Merck & Co. Inc.", "Healthcare", new BigDecimal("280000000000"), new BigDecimal("105.60")),
                new StockData("ABT", "Abbott Laboratories", "Healthcare", new BigDecimal("190000000000"), new BigDecimal("110.25")),
                new StockData("LLY", "Eli Lilly and Company", "Healthcare", new BigDecimal("550000000000"), new BigDecimal("580.20")),
                new StockData("UNH", "UnitedHealth Group", "Healthcare", new BigDecimal("480000000000"), new BigDecimal("520.45")),
                new StockData("TMO", "Thermo Fisher Scientific", "Healthcare", new BigDecimal("220000000000"), new BigDecimal("560.80")),
                new StockData("AMGN", "Amgen Inc.", "Healthcare", new BigDecimal("150000000000"), new BigDecimal("285.40")),
                new StockData("GILD", "Gilead Sciences", "Healthcare", new BigDecimal("100000000000"), new BigDecimal("85.60")),
                new StockData("BMY", "Bristol-Myers Squibb", "Healthcare", new BigDecimal("140000000000"), new BigDecimal("62.30")),

                // CONSUMER (8 actions)
                new StockData("PG", "Procter & Gamble Co.", "Consumer Defensive", new BigDecimal("350000000000"), new BigDecimal("147.80")),
                new StockData("KO", "Coca-Cola Co.", "Consumer Defensive", new BigDecimal("260000000000"), new BigDecimal("60.15")),
                new StockData("PEP", "PepsiCo Inc.", "Consumer Defensive", new BigDecimal("230000000000"), new BigDecimal("170.45")),
                new StockData("WMT", "Walmart Inc.", "Consumer Defensive", new BigDecimal("390000000000"), new BigDecimal("155.70")),
                new StockData("MCD", "McDonald's Corp.", "Consumer Cyclical", new BigDecimal("210000000000"), new BigDecimal("285.30")),
                new StockData("NKE", "Nike Inc.", "Consumer Cyclical", new BigDecimal("185000000000"), new BigDecimal("125.80")),
                new StockData("SBUX", "Starbucks Corp.", "Consumer Cyclical", new BigDecimal("120000000000"), new BigDecimal("105.60")),
                new StockData("TGT", "Target Corp.", "Consumer Defensive", new BigDecimal("75000000000"), new BigDecimal("130.25")),

                // ENERGY & INDUSTRIAL (7 actions)
                new StockData("XOM", "Exxon Mobil Corporation", "Energy", new BigDecimal("450000000000"), new BigDecimal("102.25")),
                new StockData("CVX", "Chevron Corporation", "Energy", new BigDecimal("290000000000"), new BigDecimal("150.80")),
                new StockData("COP", "ConocoPhillips", "Energy", new BigDecimal("140000000000"), new BigDecimal("115.40")),
                new StockData("SLB", "Schlumberger Ltd.", "Energy", new BigDecimal("80000000000"), new BigDecimal("52.60")),
                new StockData("BA", "Boeing Co.", "Industrial", new BigDecimal("130000000000"), new BigDecimal("210.45")),
                new StockData("CAT", "Caterpillar Inc.", "Industrial", new BigDecimal("160000000000"), new BigDecimal("245.60")),
                new StockData("MMM", "3M Company", "Industrial", new BigDecimal("58000000000"), new BigDecimal("105.80"))
        );

        Market nasdaq = marketRepository.findByCode("NASDAQ")
                .orElseThrow(() -> new RuntimeException("NASDAQ market not found"));

        List<Stock> stocks = new ArrayList<>();
        for (StockData data : stockDataList) {
            Stock stock = createStock(data, nasdaq);
            stocks.add(stock);
        }

        // Sauvegarde en lot pour meilleure performance
        List<Stock> savedStocks = stockRepository.saveAll(stocks);
        log.info("✅ {} stocks saved to database", savedStocks.size());

        // Générer les données historiques pour toute l'année 2023 (252 jours)
        generateFullYearHistoricalData(savedStocks);

        // Create order books
        createOrderBooks(savedStocks);

        log.info("🎊 Successfully imported {} stocks with full 2023 data", savedStocks.size());
    }

    private void generateFullYearHistoricalData(List<Stock> stocks) {
        List<PriceHistory> allHistory = new ArrayList<>();
        int totalTradingDays = 252; // Jours de trading en 2023

        for (Stock stock : stocks) {
            List<PriceHistory> stockHistory = generateRealisticYearHistory(stock, totalTradingDays);
            allHistory.addAll(stockHistory);

            // Log de progression
            if (allHistory.size() % 5000 == 0) {
                log.info("📈 Generated {} price records so far...", allHistory.size());
            }
        }

        // Sauvegarde par lots pour éviter les timeouts
        int batchSize = 1000;
        for (int i = 0; i < allHistory.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allHistory.size());
            List<PriceHistory> batch = allHistory.subList(i, end);
            priceHistoryRepository.saveAll(batch);
        }

        log.info("📊 Generated {} price history records for full year 2023", allHistory.size());
    }

    private List<PriceHistory> generateRealisticYearHistory(Stock stock, int totalDays) {
        List<PriceHistory> history = new ArrayList<>();
        LocalDate currentDate = LocalDate.of(2023, 1, 3); // Premier jour de trading 2023

        // Simulation de tendances réalistes
        BigDecimal basePrice = stock.getCurrentPrice();
        BigDecimal volatility = getSectorVolatility(stock.getSector());

        for (int day = 0; day < totalDays; day++) {
            if (currentDate.getDayOfWeek().getValue() <= 5) { // Jours de semaine seulement
                // Tendance générale + volatilité quotidienne
                BigDecimal randomMove = BigDecimal.valueOf((Math.random() - 0.5) * 2)
                        .multiply(volatility).multiply(basePrice);

                BigDecimal open = basePrice.add(randomMove);

                // Mouvement intraday réaliste
                BigDecimal dailyChange = BigDecimal.valueOf((Math.random() - 0.5) * 0.04)
                        .multiply(open);
                BigDecimal close = open.add(dailyChange);

                // High/Low réalistes
                BigDecimal high = open.max(close).multiply(BigDecimal.ONE.add(
                        BigDecimal.valueOf(Math.random() * 0.02)
                ));
                BigDecimal low = open.min(close).multiply(BigDecimal.ONE.subtract(
                        BigDecimal.valueOf(Math.random() * 0.02)
                ));

                // Volume réaliste basé sur la capitalisation
                Long volume = generateRealisticVolume(stock.getMarketCap());

                PriceHistory priceHistory = PriceHistory.builder()
                        .dateTime(currentDate.atTime(16, 0))
                        .openPrice(open.max(new BigDecimal("0.01")))
                        .closePrice(close.max(new BigDecimal("0.01")))
                        .highPrice(high.max(new BigDecimal("0.01")))
                        .lowPrice(low.max(new BigDecimal("0.01")))
                        .volume(volume)
                        .stock(stock)
                        .createdAt(LocalDateTime.now())
                        .build();

                history.add(priceHistory);
                basePrice = close; // Le prix de fermeture devient la base pour le jour suivant
            }
            currentDate = currentDate.plusDays(1);
        }

        return history;
    }

    private BigDecimal getSectorVolatility(String sector) {
        return switch (sector) {
            case "Technology" -> new BigDecimal("0.025");
            case "Automotive" -> new BigDecimal("0.030");
            case "Financial Services" -> new BigDecimal("0.020");
            case "Healthcare" -> new BigDecimal("0.015");
            case "Energy" -> new BigDecimal("0.028");
            case "Industrial" -> new BigDecimal("0.022");
            case "Consumer Defensive" -> new BigDecimal("0.012");
            case "Consumer Cyclical" -> new BigDecimal("0.018");
            default -> new BigDecimal("0.020");
        };
    }

    private Long generateRealisticVolume(BigDecimal marketCap) {
        BigDecimal scale = marketCap.divide(new BigDecimal("1000000000000"), 2, RoundingMode.HALF_UP);
        long baseVolume = 5_000_000L;
        long variableVolume = (long) (Math.random() * 20_000_000L);
        return baseVolume + (long) (variableVolume * scale.doubleValue());
    }

    private Stock createStock(StockData data, Market market) {
        return Stock.builder()
                .symbol(data.symbol())
                .companyName(data.companyName())
                .sector(data.sector())
                .marketCap(data.marketCap())
                .currentPrice(data.currentPrice())
                .market(market)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void createOrderBooks(List<Stock> stocks) {
        List<OrderBook> orderBooks = new ArrayList<>();

        for (Stock stock : stocks) {
            BigDecimal spread = BigDecimal.valueOf(0.01 + Math.random() * 0.05);
            BigDecimal liquidity = stock.getMarketCap()
                    .divide(new BigDecimal("1000000000"), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(1000000));

            OrderBook orderBook = OrderBook.builder()
                    .currentPrice(stock.getCurrentPrice())
                    .spread(spread)
                    .liquidity(liquidity)
                    .stock(stock)
                    .totalBidVolume((int) (Math.random() * 10000) + 1000)
                    .totalAskVolume((int) (Math.random() * 10000) + 1000)
                    .lastUpdated(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            orderBooks.add(orderBook);
        }

        orderBookRepository.saveAll(orderBooks);
        log.info("📋 Created {} order books", orderBooks.size());
    }

    // Data class
    private record StockData(String symbol, String companyName, String sector,
                             BigDecimal marketCap, BigDecimal currentPrice) {}
}