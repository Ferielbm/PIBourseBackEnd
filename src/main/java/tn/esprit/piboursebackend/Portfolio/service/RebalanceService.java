package tn.esprit.piboursebackend.Portfolio.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Portfolio.Dto.*;
import tn.esprit.piboursebackend.Portfolio.Entity.*;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot.PricingMode;
import tn.esprit.piboursebackend.Portfolio.Repository.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class RebalanceService {

    private final PortfolioRepository portfolioRepo;
    private final TargetWeightRepository targetRepo;
    private final PositionRepository positionRepo;
    private final ModelPortfolioRepository modelRepo;
    private final ModelPortfolioLineRepository modelLineRepo;
    private final MarketDataService md;
    private final NavService navService;

    public RebalanceService(PortfolioRepository p, TargetWeightRepository t, PositionRepository pos,
                            ModelPortfolioRepository m, ModelPortfolioLineRepository ml,
                            MarketDataService md, NavService nav) {
        this.portfolioRepo = p; this.targetRepo = t; this.positionRepo = pos;
        this.modelRepo = m; this.modelLineRepo = ml; this.md = md; this.navService = nav;
    }

    // -------- setTargetWeights --------
    @Transactional
    public List<TargetWeight> setTargetWeights(SetTargetWeightsRequest req) {
        var portfolio = portfolioRepo.findById(req.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        // validate sum ≈ 1.0 (allow tiny epsilon)
        BigDecimal sum = req.weights().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.subtract(BigDecimal.ONE).abs().doubleValue() > 1e-6) {
            throw new IllegalArgumentException("weights must sum to 1.0 (±1e-6). Got " + sum);
        }

        // strategy: replace existing set with given map
        targetRepo.deleteByPortfolio_Id(req.portfolioId());

        List<TargetWeight> out = new ArrayList<>();
        for (var e : req.weights().entrySet()) {
            var tw = TargetWeight.builder()
                    .portfolio(portfolio)
                    .stock(StockRef(e.getKey())) // helper below
                    .weight(e.getValue())
                    .build();
            out.add(targetRepo.save(tw));
        }
        return out;
    }

    private Stock StockRef(Long id) {
        // lightweight reference without loading the whole entity
        Stock s = new Stock();
        s.setId(id);
        return s;
    }

    // -------- proposeRebalanceTrades --------
    public List<TradeProposal> proposeRebalanceTrades(ProposeRebalanceRequest req) {
        var portfolio = portfolioRepo.findById(req.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        var base = portfolio.getBaseCurrency();
        var asOf = req.asOf();
        var tol = (req.toleranceBps() == null ? 0 : req.toleranceBps());
        BigDecimal tolDec = BigDecimal.valueOf(tol).divide(BigDecimal.valueOf(10_000), MathContext.DECIMAL64);

        // NAV (base)
        var nav = navService.computeNAV(req.portfolioId(), asOf, PricingMode.MARK_TO_MARKET).nav();

        // current MV per stock (base)
        Map<Long, BigDecimal> mvBase = new HashMap<>();
        Map<Long, Stock> stockById = new HashMap<>();

        positionRepo.findByPortfolio_Id(req.portfolioId()).forEach(pos -> {
            var s = pos.getStock();
            stockById.put(s.getId(), s);
            var price = md.getPrice(s, asOf, PricingMode.MARK_TO_MARKET);      // in stock ccy
            var fx = md.getFx(s.getCurrency(), base, asOf);                    // to base
            var qty = BigDecimal.valueOf(pos.getQuantity());
            var mv = qty.multiply(price).multiply(fx);
            mvBase.merge(s.getId(), mv, BigDecimal::add);
        });

        // current weight per stock
        Map<Long, BigDecimal> wCurr = new HashMap<>();
        for (var e : mvBase.entrySet()) {
            wCurr.put(e.getKey(), e.getValue().divide(nav.max(BigDecimal.valueOf(1e-9)), MathContext.DECIMAL64));
        }

        // target weights
        List<TargetWeight> targets = targetRepo.findByPortfolio_Id(req.portfolioId());
        if (targets.isEmpty()) return List.of(); // nothing to do

        List<TradeProposal> proposals = new ArrayList<>();

        for (var tw : targets) {
            Long stockId = tw.getStock().getId();
            var s = stockById.getOrDefault(stockId, tw.getStock()); // may be null fields if not in positions
            BigDecimal wTgt = tw.getWeight();
            BigDecimal wNow = wCurr.getOrDefault(stockId, BigDecimal.ZERO);
            BigDecimal deltaW = wTgt.subtract(wNow);

            // tolerance check
            if (deltaW.abs().compareTo(tolDec) <= 0) continue; // within tolerance → skip

            // compute delta value in base
            BigDecimal deltaValueBase = nav.multiply(deltaW);

            // price*fx to convert value delta -> quantity
            var price = md.getPrice(s, asOf, PricingMode.MARK_TO_MARKET);
            var fx    = md.getFx(s.getCurrency(), base, asOf);
            BigDecimal pxBase = price.multiply(fx);

            if (pxBase.compareTo(BigDecimal.ZERO) == 0) continue; // cannot compute qty

            BigDecimal rawQty = deltaValueBase.divide(pxBase, 8, RoundingMode.HALF_UP);
            BigDecimal qty = rawQty.abs().setScale(0, RoundingMode.DOWN); // integer shares

            if (qty.compareTo(BigDecimal.ZERO) == 0) continue;

            String side = deltaW.signum() > 0 ? "BUY" : "SELL";

            proposals.add(new TradeProposal(
                    stockId,
                    s.getSymbol(),
                    s.getCurrency(),
                    side,
                    qty,
                    deltaValueBase,
                    deltaW
            ));
        }

        // sort by largest absolute delta first
        proposals.sort(Comparator.comparing(tp -> tp.deltaValueBase().abs()));
        Collections.reverse(proposals);
        return proposals;
    }

    // -------- applyModelPortfolio --------
    @Transactional
    public List<TradeProposal> applyModelPortfolio(ApplyModelPortfolioRequest req) {
        // 1) read model weights
        var model = modelRepo.findById(req.modelId())
                .orElseThrow(() -> new IllegalArgumentException("Model not found"));
        var lines = modelLineRepo.findByModel_Id(model.getId());
        if (lines.isEmpty()) return List.of();

        // 2) convert to map<stockId, weight>
        Map<Long, BigDecimal> map = new HashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (var l : lines) { map.put(l.getStock().getId(), l.getWeight()); sum = sum.add(l.getWeight()); }

        // normalize if not exactly 1.0
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            for (var k : map.keySet()) {
                map.put(k, map.get(k).divide(sum, MathContext.DECIMAL64));
            }
        }

        // 3) set targets = model
        setTargetWeights(new SetTargetWeightsRequest(req.portfolioId(), map));

        // 4) propose trades to reach model
        return proposeRebalanceTrades(new ProposeRebalanceRequest(
                req.portfolioId(), req.asOf(), req.toleranceBps()
        ));
    }
}
