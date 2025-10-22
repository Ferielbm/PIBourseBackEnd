package tn.esprit.piboursebackend.Portfolio.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Portfolio.Dto.NavBreakdown;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot.PricingMode;
import tn.esprit.piboursebackend.Portfolio.Repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

@Service
public class NavService {

    private final PortfolioRepository portfolioRepo;
    private final PositionRepository positionRepo;
    private final CashBalanceRepository cashBalanceRepo;
    private final PortfolioSnapshotRepository snapshotRepo;
    private final MarketDataService md;

    public NavService(PortfolioRepository p, PositionRepository pos, CashBalanceRepository cb,
                      PortfolioSnapshotRepository snap, MarketDataService md) {
        this.portfolioRepo = p; this.positionRepo = pos; this.cashBalanceRepo = cb; this.snapshotRepo = snap; this.md = md;
    }

    public NavBreakdown computeNAV(Long portfolioId, Instant asOf, PricingMode mode) {
        var portfolio = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        var base = portfolio.getBaseCurrency();

        // cash in base (use new repo method)
        var cbList = cashBalanceRepo.findByPortfolio_Id(portfolioId);

        BigDecimal cashBase = BigDecimal.ZERO;
        for (var cb : cbList) {
            var fx = md.getFx(cb.getCurrency(), base, asOf);
            cashBase = cashBase.add(cb.getBalance().multiply(fx));
        }

        // positions
        var positions = positionRepo.findByPortfolio_Id(portfolioId);
        var lines = new ArrayList<NavBreakdown.Line>();
        BigDecimal mvTotal = BigDecimal.ZERO;

        for (var pos : positions) {
            var stock = pos.getStock();
            var qty = BigDecimal.valueOf(pos.getQuantity());
            var price = md.getPrice(stock, asOf, mode);            // stock currency
            var fx    = md.getFx(stock.getCurrency(), base, asOf); // to base
            var mvBase = qty.multiply(price).multiply(fx);
            mvTotal = mvTotal.add(mvBase);

            lines.add(new NavBreakdown.Line(pos.getPositionId(), stock.getSymbol(), stock.getCurrency(),
                    qty, price, fx, mvBase));
        }

        var nav = cashBase.add(mvTotal);
        return new NavBreakdown(nav, cashBase, mvTotal, lines);
    }

    @Transactional
    public PortfolioSnapshot takeSnapshot(Long portfolioId, Instant asOf, PricingMode mode) {
        var breakdown = computeNAV(portfolioId, asOf, mode);

        var snap = PortfolioSnapshot.builder()
                .portfolio(portfolioRepo.getReferenceById(portfolioId))
                .asOf(asOf)
                .pricingMode(mode)
                .nav(breakdown.nav())
                .cashBase(breakdown.cashBase())
                .marketValueTotal(breakdown.marketValueTotal())
                .detailsJson(null)
                .build();

        return snapshotRepo.save(snap);
    }
}
