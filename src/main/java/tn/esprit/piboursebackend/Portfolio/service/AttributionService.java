package tn.esprit.piboursebackend.Portfolio.service;

import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Portfolio.Repository.*;
import tn.esprit.piboursebackend.Portfolio.Entity.*;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot.PricingMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class AttributionService {

    private final NavService navService;
    private final PositionRepository posRepo;
    private final MarketDataService md;
    private final PortfolioRepository portfolioRepo;

    public AttributionService(NavService nav, PositionRepository pr, MarketDataService md, PortfolioRepository pRepo) {
        this.navService = nav; this.posRepo = pr; this.md = md; this.portfolioRepo = pRepo;
    }

    public record Line(String key, BigDecimal startWeight, BigDecimal bucketReturn, BigDecimal contribution) {}
    public static class Result {
        public BigDecimal portfolioReturn;
        public List<Line> lines = new ArrayList<>();
    }

    public Result byAsset(Long portfolioId, Instant start, Instant end) {
        var portfolio = portfolioRepo.findById(portfolioId).orElseThrow();
        var base = portfolio.getBaseCurrency();
        var mode = PricingMode.MARK_TO_MARKET;

        var positions = posRepo.findByPortfolio_Id(portfolioId);

        // compute start & end MV per asset
        Map<Long, BigDecimal> mvStart = new HashMap<>();
        Map<Long, BigDecimal> mvEnd   = new HashMap<>();

        for (var p : positions) {
            var s = p.getStock();
            var fxS = md.getFx(s.getCurrency(), base, start);
            var fxE = md.getFx(s.getCurrency(), base, end);
            var qty = BigDecimal.valueOf(p.getQuantity());

            mvStart.put(p.getPositionId(),
                    qty.multiply(md.getPrice(s, start, mode)).multiply(fxS));
            mvEnd.put(p.getPositionId(),
                    qty.multiply(md.getPrice(s, end, mode)).multiply(fxE));
        }

        var navStart = navService.computeNAV(portfolioId, start, mode).nav();
        var navEnd   = navService.computeNAV(portfolioId, end,   mode).nav();
        var portReturn = navEnd.subtract(navStart).divide(navStart, java.math.MathContext.DECIMAL64);

        var res = new Result();
        res.portfolioReturn = portReturn;

        for (var p : positions) {
            var id = p.getPositionId();
            var w0 = mvStart.get(id).divide(navStart, java.math.MathContext.DECIMAL64);
            var rb = mvEnd.get(id).subtract(mvStart.get(id))
                    .divide(mvStart.get(id).max(BigDecimal.valueOf(1e-9)), java.math.MathContext.DECIMAL64);
            var contrib = w0.multiply(rb);
            res.lines.add(new Line(p.getStock().getSymbol(), w0, rb, contrib));
        }
        return res;
    }
}
