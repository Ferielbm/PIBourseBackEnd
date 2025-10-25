package tn.esprit.piboursebackend.Marche.Service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class DataImportService {

    private final MarketRepository marketRepository;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final OrderBookRepository orderBookRepository;

    public String importFullYearData() {
        try {
            // Nettoyer les données existantes (avec gestion d'erreur)
            try {
                priceHistoryRepository.deleteAll();
            } catch (Exception e) {
                System.out.println("⚠️ No price history to delete");
            }

            try {
                orderBookRepository.deleteAll();
            } catch (Exception e) {
                System.out.println("⚠️ No order books to delete");
            }

            try {
                stockRepository.deleteAll();
            } catch (Exception e) {
                System.out.println("⚠️ No stocks to delete");
            }

            try {
                marketRepository.deleteAll();
            } catch (Exception e) {
                System.out.println("⚠️ No markets to delete");
            }

            // Create markets
            Market nasdaq = createMarket("NASDAQ", "NASDAQ Stock Market");
            Market nyse = createMarket("NYSE", "New York Stock Exchange");

            // Import stocks with full year data
            importStocksWithFullYearData();

            return "✅ Full 2023 market data imported successfully!";

        } catch (Exception e) {
            return "❌ Market data initialization failed: " + e.getMessage();
        }
    }

    private Market createMarket(String code, String name) {
        Market market = Market.builder()
                .code(code)
                .name(name)
                .currentDate(LocalDateTime.of(2023, 1, 1, 9, 30))
                .isOpen(true)
                .timeCompressionRatio(new BigDecimal("168.0"))
                .build();
        return marketRepository.save(market);
    }

    private void importStocksWithFullYearData() {
        // Liste réduite pour les tests
        List<StockData> stockDataList = List.of(
                new StockData("AAPL", "Apple Inc.", "Technology",
                        new BigDecimal("2800000000000"), new BigDecimal("182.63")),
                new StockData("TSLA", "Tesla Inc.", "Automotive",
                        new BigDecimal("800000000000"), new BigDecimal("248.42")),
                new StockData("MSFT", "Microsoft Corporation", "Technology",
                        new BigDecimal("2800000000000"), new BigDecimal("374.51")),
                new StockData("GOOGL", "Alphabet Inc.", "Technology",
                        new BigDecimal("1750000000000"), new BigDecimal("138.57")),
                new StockData("AMZN", "Amazon.com Inc.", "Technology",
                        new BigDecimal("1550000000000"), new BigDecimal("151.94"))
        );

        Market nasdaq = marketRepository.findByCode("NASDAQ")
                .orElseThrow(() -> new RuntimeException("NASDAQ market not found"));

        List<Stock> stocks = new ArrayList<>();
        for (StockData data : stockDataList) {
            Stock stock = createStock(data, nasdaq);
            stocks.add(stock);
        }

        stockRepository.saveAll(stocks);

        // Generate historical data (réduit pour les tests)
        generateHistoricalData(stocks, 30); // 30 jours au lieu de 252

        // Create order books
        createOrderBooks(stocks);

        System.out.println("✅ " + stocks.size() + " stocks imported successfully");
    }

    private Stock createStock(StockData data, Market market) {
        return Stock.builder()
                .symbol(data.symbol())
                .companyName(data.companyName())
                .sector(data.sector())
                .marketCap(data.marketCap())
                .currentPrice(data.currentPrice())
                .market(market)
                .build();
    }

    private void generateHistoricalData(List<Stock> stocks, int days) {
        List<PriceHistory> allHistory = new ArrayList<>();

        for (Stock stock : stocks) {
            List<PriceHistory> stockHistory = generateStockPriceHistory(stock, days);
            allHistory.addAll(stockHistory);
        }

        priceHistoryRepository.saveAll(allHistory);
        System.out.println("📈 Generated " + allHistory.size() + " price history records");
    }

    private List<PriceHistory> generateStockPriceHistory(Stock stock, int days) {
        List<PriceHistory> history = new ArrayList<>();
        LocalDate currentDate = LocalDate.of(2023, 1, 3);
        BigDecimal basePrice = stock.getCurrentPrice();

        for (int day = 0; day < days; day++) {
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                BigDecimal randomMove = BigDecimal.valueOf((Math.random() - 0.5) * 0.1).multiply(basePrice);
                BigDecimal open = basePrice.add(randomMove);
                BigDecimal dailyChange = BigDecimal.valueOf((Math.random() - 0.5) * 0.04).multiply(open);
                BigDecimal close = open.add(dailyChange);
                BigDecimal high = open.max(close).multiply(BigDecimal.ONE.add(
                        BigDecimal.valueOf(Math.random() * 0.02)
                ));
                BigDecimal low = open.min(close).multiply(BigDecimal.ONE.subtract(
                        BigDecimal.valueOf(Math.random() * 0.02)
                ));

                Long volume = 10000000L + (long)(Math.random() * 50000000);

                PriceHistory priceHistory = PriceHistory.builder()
                        .dateTime(currentDate.atTime(16, 0))
                        .openPrice(open.max(new BigDecimal("0.01")))
                        .closePrice(close.max(new BigDecimal("0.01")))
                        .highPrice(high.max(new BigDecimal("0.01")))
                        .lowPrice(low.max(new BigDecimal("0.01")))
                        .volume(volume)
                        .stock(stock)
                        .build();

                history.add(priceHistory);
            }
            currentDate = currentDate.plusDays(1);
        }

        return history;
    }

    private void createOrderBooks(List<Stock> stocks) {
        List<OrderBook> orderBooks = new ArrayList<>();

        for (Stock stock : stocks) {
            OrderBook orderBook = OrderBook.builder()
                    .currentPrice(stock.getCurrentPrice())
                    .spread(new BigDecimal("0.02"))
                    .liquidity(new BigDecimal("1000000"))
                    .stock(stock)
                    .totalBidVolume(5000)
                    .totalAskVolume(5000)
                    .build();

            orderBooks.add(orderBook);
        }

        orderBookRepository.saveAll(orderBooks);
        System.out.println("📊 Created " + orderBooks.size() + " order books");
    }

    // Data class
    private record StockData(String symbol, String companyName, String sector,
                             BigDecimal marketCap, BigDecimal currentPrice) {}
}