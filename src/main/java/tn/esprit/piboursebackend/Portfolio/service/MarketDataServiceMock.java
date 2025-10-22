package tn.esprit.piboursebackend.Portfolio.service;

import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class MarketDataServiceMock implements MarketDataService {
    public BigDecimal getPrice(Stock stock, Instant asOf, PortfolioSnapshot.PricingMode mode) {
        return stock.getLastPrice() != null ? stock.getLastPrice() : BigDecimal.ZERO; // adjust to your Stock
    }
    public BigDecimal getFx(String from, String to, Instant asOf) {
        return from.equals(to) ? BigDecimal.ONE : BigDecimal.valueOf(3.10); // mock TND per foreign
    }
}