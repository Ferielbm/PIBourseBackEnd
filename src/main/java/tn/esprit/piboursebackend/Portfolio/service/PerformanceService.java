package tn.esprit.piboursebackend.Portfolio.service;

import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Portfolio.Entity.CashFlow;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot;
import tn.esprit.piboursebackend.Portfolio.Repository.CashFlowRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.PortfolioSnapshotRepository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PerformanceService {

    // === make the cash point type CLASS-SCOPED ===
    public static record CashPoint(Instant t, BigDecimal amount) {}

    private final PortfolioSnapshotRepository snapRepo; // (kept for future use)
    private final CashFlowRepository cashRepo;
    private final NavService navService;

    public PerformanceService(PortfolioSnapshotRepository s, CashFlowRepository c, NavService nav) {
        this.snapRepo = s; this.cashRepo = c; this.navService = nav;
    }

    // ---------- TWR ----------
    public BigDecimal performanceTWR(Long portfolioId, Instant start, Instant end) {
        var cf = cashRepo.findAll().stream()
                .filter(x -> x.getPortfolio().getId().equals(portfolioId))
                .filter(x -> !x.getAsOf().isBefore(start) && x.getAsOf().isBefore(end))
                .sorted(Comparator.comparing(CashFlow::getAsOf))
                .toList();

        var pricing = PortfolioSnapshot.PricingMode.MARK_TO_MARKET;

        Instant t0 = start;
        BigDecimal twrProduct = BigDecimal.ONE;

        for (var flow : cf) {
            Instant t1 = flow.getAsOf();
            var navStart = navService.computeNAV(portfolioId, t0, pricing).nav();
            var navEnd   = navService.computeNAV(portfolioId, t1, pricing).nav();

            BigDecimal cfAmountSigned = signed(flow);

            // r = (NAV_end - NAV_start - CF_in_period) / NAV_start
            BigDecimal r = navEnd.subtract(navStart).subtract(cfAmountSigned)
                    .divide(navStart.max(BigDecimal.valueOf(1e-9)), MathContext.DECIMAL64);
            twrProduct = twrProduct.multiply(BigDecimal.ONE.add(r));
            t0 = t1; // next period starts at the CF time
        }

        // last sub-period up to end (no CF at end)
        var navStart = navService.computeNAV(portfolioId, t0, pricing).nav();
        var navEnd   = navService.computeNAV(portfolioId, end, pricing).nav();
        BigDecimal rLast = navEnd.subtract(navStart)
                .divide(navStart.max(BigDecimal.valueOf(1e-9)), MathContext.DECIMAL64);
        twrProduct = twrProduct.multiply(BigDecimal.ONE.add(rLast));

        return twrProduct.subtract(BigDecimal.ONE);
    }

    private BigDecimal signed(CashFlow cf) {
        var amt = cf.getAmount();
        return switch (cf.getType()) {
            case WITHDRAWAL, FEE, TAX, TRANSFER_OUT -> amt.negate();
            default -> amt;
        };
    }

    // ---------- MWR (XIRR-like) ----------
    public BigDecimal performanceMWR(Long portfolioId, Instant start, Instant end) {
        var pricing = PortfolioSnapshot.PricingMode.MARK_TO_MARKET;

        var navStart = navService.computeNAV(portfolioId, start, pricing).nav();
        var navEnd   = navService.computeNAV(portfolioId, end,   pricing).nav();

        // Build cash flow series: start NAV as negative, intermediate CF signed, end NAV as positive
        List<CashPoint> series = new ArrayList<>();
        series.add(new CashPoint(start, navStart.negate()));

        cashRepo.findAll().stream()
                .filter(x -> x.getPortfolio().getId().equals(portfolioId))
                .filter(x -> x.getAsOf().isAfter(start) && x.getAsOf().isBefore(end))
                .sorted(Comparator.comparing(CashFlow::getAsOf))
                .forEach(x -> series.add(new CashPoint(x.getAsOf(), signed(x))));

        series.add(new CashPoint(end, navEnd));

        // Newton solver (guarded)
        BigDecimal r = BigDecimal.valueOf(0.10); // 10% initial guess
        for (int i=0; i<50; i++) {
            var f  = npv(series, r, start);
            var df = dNpv(series, r, start);
            if (df.abs().compareTo(BigDecimal.valueOf(1e-12)) < 0) break;
            var step = f.divide(df, MathContext.DECIMAL64);
            r = r.subtract(step);
            if (step.abs().doubleValue() < 1e-10) break;
        }
        return r; // annualized
    }

    private BigDecimal yearFrac(Instant start, Instant t) {
        double days = ChronoUnit.DAYS.between(start, t);
        return BigDecimal.valueOf(days / 365.25d);
    }

    private BigDecimal npv(List<CashPoint> series, BigDecimal r, Instant start) {
        BigDecimal sum = BigDecimal.ZERO;
        for (var c : series) {
            var t = yearFrac(start, c.t());
            double pow = Math.pow(1.0 + r.doubleValue(), t.doubleValue());
            sum = sum.add(c.amount().divide(BigDecimal.valueOf(pow), MathContext.DECIMAL64));
        }
        return sum;
    }

    private BigDecimal dNpv(List<CashPoint> series, BigDecimal r, Instant start) {
        BigDecimal sum = BigDecimal.ZERO;
        for (var c : series) {
            var t = yearFrac(start, c.t()).doubleValue();
            double base = 1.0 + r.doubleValue();
            double denom = Math.pow(base, t + 1e-12);
            double term = -t * c.amount().doubleValue() / denom;
            sum = sum.add(BigDecimal.valueOf(term));
        }
        return sum;
    }
}
