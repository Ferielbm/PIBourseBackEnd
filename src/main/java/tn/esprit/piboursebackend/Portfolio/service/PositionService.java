package tn.esprit.piboursebackend.Portfolio.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Portfolio.Dto.*;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;
import tn.esprit.piboursebackend.Portfolio.Entity.PositionLot;
import tn.esprit.piboursebackend.Portfolio.Repository.PortfolioRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.PositionLotRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.PositionRepository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

@Service
public class PositionService {
    private final PortfolioRepository portfolioRepo;
    private final PositionRepository positionRepo;
    private final PositionLotRepository lotRepo;
    private final MarketDataService md;
    public PositionService(PortfolioRepository p, PositionRepository pr, PositionLotRepository lr, MarketDataService md) {
        this.portfolioRepo = p; this.positionRepo = pr; this.lotRepo = lr; this.md = md;
    }
    public List<Position> getAllPositions() {
        return positionRepo.findAll();
    }

    public Position createPosition(Position Position) {
        return positionRepo.save(Position);
    }
    public void deletePosition(Long id) {
        positionRepo.deleteById(id);
    }
    @Transactional
    public PositionView applyFill(FillRequest f) {
        if (f == null) throw new IllegalArgumentException("fill is required");
        if (f.portfolioId() == null) throw new IllegalArgumentException("portfolioId is required");
        if (f.stockId() == null) throw new IllegalArgumentException("stockId is required");
        if (f.side() == null) throw new IllegalArgumentException("side is required");
        if (f.quantity() == null || f.quantity() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (f.price() == null || f.price().signum() <= 0) throw new IllegalArgumentException("price must be > 0");
        var portfolio = portfolioRepo.findById(f.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        // load or create position
        var pos = positionRepo.findByPortfolio_IdAndStock_Id(f.portfolioId(), f.stockId())
                .orElseGet(() -> {
                    var p = Position.builder()
                            .portfolio(portfolio)
                            .stock(stockRef(f.stockId()))
                            .quantity(0)
                            .averagePrice(ZERO)
                            .build();
                    return positionRepo.save(p);
                });

        if (f.side() == Side.BUY) {
            // weighted average price
            int oldQty = Optional.ofNullable(pos.getQuantity()).orElse(0);
            BigDecimal oldAvg = Optional.ofNullable(pos.getAveragePrice()).orElse(ZERO);
            int newQty = oldQty + f.quantity();

            BigDecimal newAvg = (oldAvg.multiply(BigDecimal.valueOf(oldQty))
                    .add(f.price().multiply(BigDecimal.valueOf(f.quantity()))))
                    .divide(BigDecimal.valueOf(newQty), MathContext.DECIMAL64);

            pos.setQuantity(newQty);
            pos.setAveragePrice(newAvg);

            // create a lot
            var lot = PositionLot.builder()
                    .position(pos)
                    .asOf(Optional.ofNullable(f.asOf()).orElse(Instant.now()))
                    .qtyOriginal(f.quantity())
                    .qtyRemaining(f.quantity())
                    .price(f.price())
                    .build();
            lotRepo.save(lot);

        } else { // SELL
            int sellQty = f.quantity();
            if (sellQty <= 0) throw new IllegalArgumentException("quantity must be > 0");
            int oldQty = Optional.ofNullable(pos.getQuantity()).orElse(0);
            if (sellQty > oldQty) throw new IllegalArgumentException("cannot sell more than position qty");

            // default close lots FIFO for inventory (we can add parameter later)
            var lots = lotRepo.findByPosition_PositionIdOrderByAsOfAsc(pos.getPositionId());
            int remaining = sellQty;
            for (var lot : lots) {
                if (remaining == 0) break;
                int c = Math.min(remaining, lot.getQtyRemaining());
                lot.setQtyRemaining(lot.getQtyRemaining() - c);
                remaining -= c;
            }
            lotRepo.saveAll(lots);

            pos.setQuantity(oldQty - sellQty);

            // Keep averagePrice unchanged for AVERAGE accounting.
            // If you want to re-average under inventory methods, do it in computePnL instead.
        }

        var saved = positionRepo.save(pos);
        var s = saved.getStock();
        // touch fields to ensure init
        s.getId(); s.getSymbol();
         return new PositionView(
                saved.getPositionId(),
                s.getId(),
                s.getSymbol(),
                saved.getQuantity(),
                saved.getAveragePrice()
        );
    }

    // ---------- 2) markToMarket ----------
    public MtMResponse markToMarket(Long positionId, Instant asOf) {
        var pos = positionRepo.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));

        var stock = pos.getStock();
        var px = md.getPrice(stock, asOf, PortfolioSnapshot.PricingMode.MARK_TO_MARKET);
        var qty = Optional.ofNullable(pos.getQuantity()).orElse(0);
        BigDecimal mv = px.multiply(BigDecimal.valueOf(qty));
        BigDecimal unreal = px.subtract(pos.getAveragePrice()).multiply(BigDecimal.valueOf(qty));

        return new MtMResponse(
                pos.getPositionId(), qty, pos.getAveragePrice(), px, mv, unreal
        );
    }

    // ---------- 3) computePnL ----------
    public PnlResponse computePnLForPosition(Long positionId, Instant start, Instant end, CostMethod method) {
        var pos = positionRepo.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        return computePnLInternal(pos, start, end, method);
    }

    public PnlResponse computePnLForPortfolio(Long portfolioId, Instant start, Instant end, CostMethod method) {
        var positions = positionRepo.findByPortfolio_Id(portfolioId);
        BigDecimal realized = ZERO;
        BigDecimal unrealized = ZERO;
        for (var p : positions) {
            var r = computePnLInternal(p, start, end, method);
            realized = realized.add(r.realizedPnl());
            unrealized = unrealized.add(r.unrealizedPnl());
        }
        return new PnlResponse(realized, unrealized);
    }

    private PnlResponse computePnLInternal(Position pos, Instant start, Instant end, CostMethod method) {
        // Inventory (lots) as of 'end'
        List<PositionLot> lotsFifo = lotRepo.findByPosition_PositionIdOrderByAsOfAsc(pos.getPositionId());

        // Market price at end for unrealized
        var pxEnd = md.getPrice(pos.getStock(), end, PortfolioSnapshot.PricingMode.MARK_TO_MARKET);

        BigDecimal realized = ZERO;
        BigDecimal unrealized = ZERO;

        switch (method) {
            case AVERAGE -> {
                // Realized PnL from reductions during the window = (sellPrice - avg) * qtySold
                // NOTE: Needs fills in the window. If you don’t persist fills, approximate unrealized only:
                unrealized = pxEnd.subtract(pos.getAveragePrice())
                        .multiply(BigDecimal.valueOf(Optional.ofNullable(pos.getQuantity()).orElse(0)));
            }
            case FIFO, LIFO -> {
                // For realized in [start,end], you need fills/trades stream over time.
                // If you don’t persist fills yet, we compute UNREALIZED precisely per remaining lots,
                // and realized remains 0 (or you can add a Fill table later).
                List<PositionLot> scan = method== CostMethod.FIFO ? lotsFifo
                        : reverse(lotsFifo);
                int rem = Optional.ofNullable(pos.getQuantity()).orElse(0);
                for (var lot : scan) {
                    int qRem = Math.min(rem, lot.getQtyRemaining());
                    if (qRem <= 0) continue;
                    BigDecimal lotUnreal = pxEnd.subtract(lot.getPrice())
                            .multiply(BigDecimal.valueOf(qRem));
                    unrealized = unrealized.add(lotUnreal);
                    rem -= qRem;
                    if (rem == 0) break;
                }
            }
        }
        return new PnlResponse(realized, unrealized);
    }

    private List<PositionLot> reverse(List<PositionLot> in) {
        var out = new ArrayList<>(in);
        Collections.reverse(out);
        return out;
    }

    private Stock stockRef(Long id) {
        var s = new Stock();
        s.setId(id);
        return s;
    }

    // ---------- 4) lotManagement ----------
    public List<LotView> lotManagement(Long positionId) {
        var lots = lotRepo.findByPosition_PositionIdOrderByAsOfAsc(positionId);
        List<LotView> out = new ArrayList<>();
        for (var l : lots) {
            out.add(new LotView(l.getId(), l.getAsOf(), l.getQtyOriginal(), l.getQtyRemaining(), l.getPrice()));
        }
        return out;
    }
}
