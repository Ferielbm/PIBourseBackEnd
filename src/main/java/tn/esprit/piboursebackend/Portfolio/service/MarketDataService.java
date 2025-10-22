package tn.esprit.piboursebackend.Portfolio.service;

import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot.PricingMode;
import java.math.BigDecimal;
import java.time.Instant;

public interface MarketDataService {
    BigDecimal getPrice(Stock stock, Instant asOf, PricingMode mode);  // price in stock currency
    BigDecimal getFx(String fromCcy, String toCcy, Instant asOf);      // FX rate: 1 fromCcy = ? toCcy
}
