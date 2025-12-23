package tn.esprit.piboursebackend.Marche.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Marche.Entity.Market;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import tn.esprit.piboursebackend.Marche.Repository.MarketRepository;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Marche.Repository.PriceHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketDataService {

    private final MarketRepository marketRepository;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // Market operations
    public List<Market> getAllMarkets() {
        return marketRepository.findAll();
    }

    public Optional<Market> getMarketByCode(String code) {
        return marketRepository.findByCode(code);
    }

    public Market createMarket(Market market) {
        return marketRepository.save(market);
    }

    public Market updateMarketStatus(String code, Boolean isOpen) {
        Market market = marketRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Market not found: " + code));
        market.setIsOpen(isOpen);
        market.setCurrentDate(LocalDateTime.now());
        return marketRepository.save(market);
    }

    // Stock operations
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }

    public List<Stock> getStocksBySector(String sector) {
        return stockRepository.findBySector(sector);
    }

    public List<Stock> getStocksByMarket(String marketCode) {
        return stockRepository.findByMarket_Code(marketCode);
    }

    public List<Stock> getStocksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return stockRepository.findByPriceRange(minPrice, maxPrice);
    }

    public Stock updateStockPrice(String symbol, BigDecimal newPrice) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        stock.setCurrentPrice(newPrice);
        return stockRepository.save(stock);
    }

    // Price History operations
    public List<PriceHistory> getStockPriceHistory(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return priceHistoryRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
        } else {
            return priceHistoryRepository.findByStock_SymbolOrderByDateTime(symbol);
        }
    }

    public Optional<PriceHistory> getLatestPrice(String symbol) {
        return priceHistoryRepository.findLatestBySymbol(symbol);
    }

    public PriceHistory addPriceHistory(PriceHistory priceHistory) {
        return priceHistoryRepository.save(priceHistory);
    }

    public List<String> getAllSectors() {
        return stockRepository.findAllSectors();
    }

    public Long getOpenMarketsCount() {
        return marketRepository.countOpenMarkets();
    }
}