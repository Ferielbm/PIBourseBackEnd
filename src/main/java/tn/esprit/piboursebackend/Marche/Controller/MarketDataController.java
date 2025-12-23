package tn.esprit.piboursebackend.Marche.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Marche.Entity.Market;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import tn.esprit.piboursebackend.Marche.Service.DataImportService;
import tn.esprit.piboursebackend.Marche.Service.MarketDataService;
import tn.esprit.piboursebackend.Marche.Service.MarketStatisticsService;
import tn.esprit.piboursebackend.Marche.Service.TimeAcceleratorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final DataImportService dataImportService;
    private final MarketStatisticsService statisticsService;
    private final TimeAcceleratorService timeAcceleratorService;

    // === DATA INITIALIZATION ===
    @PostMapping("/init-full-data")
    public ResponseEntity<Map<String, Object>> initializeFullYearData() {
        log.info("🚀 Initializing full year market data...");
        try {
            String result = dataImportService.importFullYearData();
            log.info("✅ Data initialization successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", result,
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("❌ Data initialization failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Data initialization failed: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // === MARKET ENDPOINTS ===
    @GetMapping("/markets")
    public ResponseEntity<List<Market>> getAllMarkets() {
        log.debug("📋 Fetching all markets");
        return ResponseEntity.ok(marketDataService.getAllMarkets());
    }

    @GetMapping("/markets/{code}")
    public ResponseEntity<Market> getMarketByCode(@PathVariable String code) {
        log.debug("🔍 Fetching market by code: {}", code);
        return marketDataService.getMarketByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/markets/{code}/status")
    public ResponseEntity<Market> updateMarketStatus(
            @PathVariable String code,
            @RequestParam Boolean isOpen) {
        log.info("🔄 Updating market status: {} -> {}", code, isOpen);
        return ResponseEntity.ok(marketDataService.updateMarketStatus(code, isOpen));
    }

    // === STOCK ENDPOINTS ===
    @GetMapping("/stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        log.debug("📊 Fetching all stocks");
        return ResponseEntity.ok(marketDataService.getAllStocks());
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<Stock> getStockBySymbol(@PathVariable String symbol) {
        log.debug("🔍 Fetching stock by symbol: {}", symbol);
        return marketDataService.getStockBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks/sector/{sector}")
    public ResponseEntity<List<Stock>> getStocksBySector(@PathVariable String sector) {
        log.debug("🏷️ Fetching stocks by sector: {}", sector);
        return ResponseEntity.ok(marketDataService.getStocksBySector(sector));
    }

    @GetMapping("/stocks/market/{marketCode}")
    public ResponseEntity<List<Stock>> getStocksByMarket(@PathVariable String marketCode) {
        log.debug("🏛️ Fetching stocks by market: {}", marketCode);
        return ResponseEntity.ok(marketDataService.getStocksByMarket(marketCode));
    }

    @GetMapping("/stocks/price-range")
    public ResponseEntity<List<Stock>> getStocksByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.debug("💰 Fetching stocks in price range: {} - {}", minPrice, maxPrice);
        return ResponseEntity.ok(marketDataService.getStocksByPriceRange(minPrice, maxPrice));
    }

    @PutMapping("/stocks/{symbol}/price")
    public ResponseEntity<Stock> updateStockPrice(
            @PathVariable String symbol,
            @RequestParam BigDecimal newPrice) {
        log.info("🔄 Updating stock price: {} -> {}", symbol, newPrice);
        return ResponseEntity.ok(marketDataService.updateStockPrice(symbol, newPrice));
    }

    // === PRICE HISTORY ENDPOINTS ===
    @GetMapping("/stocks/{symbol}/history")
    public ResponseEntity<List<PriceHistory>> getStockHistory(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("📈 Fetching price history for: {} from {} to {}", symbol, startDate, endDate);
        List<PriceHistory> history = marketDataService.getStockPriceHistory(symbol, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stocks/{symbol}/latest-price")
    public ResponseEntity<PriceHistory> getLatestPrice(@PathVariable String symbol) {
        log.debug("🕒 Fetching latest price for: {}", symbol);
        return marketDataService.getLatestPrice(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks/{symbol}/date/{date}")
    public ResponseEntity<PriceHistory> getStockPriceByDate(
            @PathVariable String symbol,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.debug("📅 Fetching stock price for: {} on {}", symbol, date);
        LocalDateTime startOfDay = date.atTime(0, 0);
        LocalDateTime endOfDay = date.atTime(23, 59);

        List<PriceHistory> history = marketDataService.getStockPriceHistory(symbol, startOfDay, endOfDay);
        return history.isEmpty() ?
                ResponseEntity.notFound().build() :
                ResponseEntity.ok(history.get(0));
    }

    // === STATISTICS ENDPOINTS ===
    @GetMapping("/stocks/{symbol}/performance")
    public ResponseEntity<Map<String, Object>> getStockPerformance(@PathVariable String symbol) {
        log.debug("📊 Fetching performance for: {}", symbol);
        return ResponseEntity.ok(statisticsService.getStockPerformance(symbol));
    }

    @GetMapping("/stocks/top-performers")
    public ResponseEntity<List<Map<String, Object>>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("🏆 Fetching top {} performers", limit);
        return ResponseEntity.ok(statisticsService.getTopPerformers(limit));
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        log.debug("🌐 Fetching market overview");
        return ResponseEntity.ok(statisticsService.getMarketOverview());
    }

    // === UTILITY ENDPOINTS ===
    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getAllSectors() {
        log.debug("🏷️ Fetching all sectors");
        return ResponseEntity.ok(marketDataService.getAllSectors());
    }

    @GetMapping("/open-markets-count")
    public ResponseEntity<Long> getOpenMarketsCount() {
        log.debug("🔓 Fetching open markets count");
        return ResponseEntity.ok(marketDataService.getOpenMarketsCount());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("❤️ Health check");
        long totalStocks = marketDataService.getAllStocks().size();
        long openMarkets = marketDataService.getOpenMarketsCount();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "totalStocks", totalStocks,
                "openMarkets", openMarkets,
                "timestamp", LocalDateTime.now()
        ));
    }

    // === TIME MANAGEMENT ENDPOINTS ===
    @PostMapping("/time/start")
    public ResponseEntity<Map<String, String>> startTimeAcceleration() {
        log.info("⏰ Starting time acceleration");
        timeAcceleratorService.startTimeAcceleration();
        return ResponseEntity.ok(Map.of(
                "message", "⏰ Time acceleration started - 12 months will simulate in 1 hour",
                "compression", timeAcceleratorService.getTimeCompressionInfo()
        ));
    }

    @PostMapping("/time/stop")
    public ResponseEntity<Map<String, String>> stopTimeAcceleration() {
        log.info("⏹️ Stopping time acceleration");
        timeAcceleratorService.stopTimeAcceleration();
        return ResponseEntity.ok(Map.of("message", "⏹️ Time acceleration stopped"));
    }

    @GetMapping("/time/status")
    public ResponseEntity<Map<String, Object>> getTimeStatus() {
        log.debug("🕒 Fetching time status");
        return ResponseEntity.ok(Map.of(
                "active", timeAcceleratorService.isTimeAccelerationActive(),
                "currentGameTime", timeAcceleratorService.getCurrentGameTime(),
                "compressionInfo", timeAcceleratorService.getTimeCompressionInfo()
        ));
    }

    @PostMapping("/time/set")
    public ResponseEntity<Map<String, String>> setGameTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime) {
        log.info("🕒 Setting game time to: {}", newTime);
        timeAcceleratorService.setGameTime(newTime);
        return ResponseEntity.ok(Map.of("message", "Game time set to: " + newTime));
    }
}