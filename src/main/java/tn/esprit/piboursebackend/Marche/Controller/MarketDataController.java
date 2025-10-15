package tn.esprit.piboursebackend.Marche.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Marche.Entity.Market;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import tn.esprit.piboursebackend.Marche.Service.DataImportService;
import tn.esprit.piboursebackend.Marche.Service.MarketDataService;
import tn.esprit.piboursebackend.Marche.Service.MarketStatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final DataImportService dataImportService;
    private final MarketStatisticsService statisticsService;

    // === DATA INITIALIZATION ===
    @PostMapping("/init-full-data")
    public ResponseEntity<Map<String, String>> initializeFullYearData() {
        String result = dataImportService.importFullYearData();
        return ResponseEntity.ok(Map.of("message", result));
    }

    // === MARKET ENDPOINTS ===
    @GetMapping("/markets")
    public ResponseEntity<List<Market>> getAllMarkets() {
        return ResponseEntity.ok(marketDataService.getAllMarkets());
    }

    @GetMapping("/markets/{code}")
    public ResponseEntity<Market> getMarketByCode(@PathVariable String code) {
        return marketDataService.getMarketByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/markets/{code}/status")
    public ResponseEntity<Market> updateMarketStatus(
            @PathVariable String code,
            @RequestParam Boolean isOpen) {
        return ResponseEntity.ok(marketDataService.updateMarketStatus(code, isOpen));
    }

    // === STOCK ENDPOINTS ===
    @GetMapping("/stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(marketDataService.getAllStocks());
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<Stock> getStockBySymbol(@PathVariable String symbol) {
        return marketDataService.getStockBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks/sector/{sector}")
    public ResponseEntity<List<Stock>> getStocksBySector(@PathVariable String sector) {
        return ResponseEntity.ok(marketDataService.getStocksBySector(sector));
    }

    @GetMapping("/stocks/market/{marketCode}")
    public ResponseEntity<List<Stock>> getStocksByMarket(@PathVariable String marketCode) {
        return ResponseEntity.ok(marketDataService.getStocksByMarket(marketCode));
    }

    @GetMapping("/stocks/price-range")
    public ResponseEntity<List<Stock>> getStocksByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(marketDataService.getStocksByPriceRange(minPrice, maxPrice));
    }

    @PutMapping("/stocks/{symbol}/price")
    public ResponseEntity<Stock> updateStockPrice(
            @PathVariable String symbol,
            @RequestParam BigDecimal newPrice) {
        return ResponseEntity.ok(marketDataService.updateStockPrice(symbol, newPrice));
    }

    // === PRICE HISTORY ENDPOINTS ===
    @GetMapping("/stocks/{symbol}/history")
    public ResponseEntity<List<PriceHistory>> getStockHistory(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<PriceHistory> history = marketDataService.getStockPriceHistory(symbol, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stocks/{symbol}/latest-price")
    public ResponseEntity<PriceHistory> getLatestPrice(@PathVariable String symbol) {
        return marketDataService.getLatestPrice(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks/{symbol}/date/{date}")
    public ResponseEntity<PriceHistory> getStockPriceByDate(
            @PathVariable String symbol,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

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
        return ResponseEntity.ok(statisticsService.getStockPerformance(symbol));
    }

    @GetMapping("/stocks/top-performers")
    public ResponseEntity<List<Map<String, Object>>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopPerformers(limit));
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        return ResponseEntity.ok(statisticsService.getMarketOverview());
    }

    // === UTILITY ENDPOINTS ===
    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getAllSectors() {
        return ResponseEntity.ok(marketDataService.getAllSectors());
    }

    @GetMapping("/open-markets-count")
    public ResponseEntity<Long> getOpenMarketsCount() {
        return ResponseEntity.ok(marketDataService.getOpenMarketsCount());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        long totalStocks = marketDataService.getAllStocks().size();
        long openMarkets = marketDataService.getOpenMarketsCount();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "totalStocks", totalStocks,
                "openMarkets", openMarkets,
                "timestamp", LocalDateTime.now()
        ));
    }
}