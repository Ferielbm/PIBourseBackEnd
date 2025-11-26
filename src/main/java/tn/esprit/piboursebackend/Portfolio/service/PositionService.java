package tn.esprit.piboursebackend.Portfolio.service;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Portfolio.Dto.*;
import tn.esprit.piboursebackend.Portfolio.Entity.*;
import tn.esprit.piboursebackend.Portfolio.Repository.PortfolioRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.PositionLotRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.PositionRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.TradeExecutionRepository;

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
    private final TradeExecutionRepository tradeExecutionRepo;
    private final CashService cashService;
    private final StockRepository stockRepo;
    public PositionService(PortfolioRepository p, PositionRepository pr, PositionLotRepository lr, MarketDataService md, TradeExecutionRepository tradeExecutionRepo, CashService cashService, StockRepository stockRepo) {
        this.portfolioRepo = p; this.positionRepo = pr; this.lotRepo = lr; this.md = md;
        this.tradeExecutionRepo = tradeExecutionRepo;
        this.cashService = cashService;
        this.stockRepo = stockRepo;
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
    private Stock fetchStockFull(Long stockId) {
        return stockRepo.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + stockId));
    }
    @Transactional
    public PositionView applyFillWithReservation(FillRequest f) {
        if (f == null) throw new IllegalArgumentException("fill is required");
        if (f.getPortfolioId() == null) throw new IllegalArgumentException("portfolioId is required");
        if (f.getStockId() == null) throw new IllegalArgumentException("stockId is required");
        if (f.getSide() == null) throw new IllegalArgumentException("side is required");
        if (f.getQuantity() == null || f.getQuantity() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (f.getPrice() == null || f.getPrice().signum() <= 0) throw new IllegalArgumentException("price must be > 0");
        if (f.getExternalTradeId() == null || f.getExternalTradeId().isBlank()) throw new IllegalArgumentException("externalTradeId is required");

        // 1️⃣ Idempotency check
        if (tradeExecutionRepo.existsByExternalTradeId(f.getExternalTradeId())) {
            var existingPos = positionRepo.findByPortfolio_IdAndStock_Id(f.getPortfolioId(), f.getStockId())
                    .orElseThrow(() -> new IllegalStateException("Execution already processed but position missing"));
            var s = existingPos.getStock();
            return new PositionView(existingPos.getPositionId(), s.getId(), s.getSymbol(),
                    existingPos.getQuantity(), existingPos.getAveragePrice());
        }

        var portfolio = portfolioRepo.findById(f.getPortfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        // 2️⃣ Lock or create Position (pessimistic)
        Position pos = positionRepo.findByPortfolio_IdAndStock_IdForUpdate(f.getPortfolioId(), f.getStockId())
                .orElseGet(() -> {
                    Position newPos = Position.builder()
                            .portfolio(portfolio)
                            .stock(fetchStockFull(f.getStockId()))
                            .quantity(0)
                            .reservedQuantity(0)
                            .averagePrice(ZERO)
                            .build();
                    try {
                        newPos = positionRepo.save(newPos);
                        return positionRepo.findByPortfolio_IdAndStock_IdForUpdate(f.getPortfolioId(), f.getStockId())
                                .orElse(newPos);
                    } catch (DataIntegrityViolationException ex) {
                        return positionRepo.findByPortfolio_IdAndStock_IdForUpdate(f.getPortfolioId(), f.getStockId())
                                .orElseThrow(() -> new IllegalStateException("Failed to acquire or create position", ex));
                    }
                });

        Instant executedAt = Optional.ofNullable(f.getAsOf()).orElse(Instant.now());
        BigDecimal total = f.getPrice().multiply(BigDecimal.valueOf(f.getQuantity()));
        String tradeCcy = Optional.ofNullable(f.getCurrency())
                .orElse(Optional.ofNullable(pos.getStock().getCurrency()).orElse(portfolio.getBaseCurrency()));
        BigDecimal fx = md.getFx(tradeCcy, portfolio.getBaseCurrency(), executedAt);
        BigDecimal cashImpactBase = total.multiply(fx);

        if (f.getSide() == Side.BUY) {
            // BUY logic
            int oldQty = Optional.ofNullable(pos.getQuantity()).orElse(0);
            BigDecimal oldAvg = Optional.ofNullable(pos.getAveragePrice()).orElse(ZERO);
            int newQty = oldQty + f.getQuantity();
            BigDecimal newAvg = (oldAvg.multiply(BigDecimal.valueOf(oldQty))
                    .add(f.getPrice().multiply(BigDecimal.valueOf(f.getQuantity()))))
                    .divide(BigDecimal.valueOf(newQty), MathContext.DECIMAL64);
            pos.setQuantity(newQty);
            pos.setAveragePrice(newAvg);

            PositionLot lot = PositionLot.builder()
                    .position(pos)
                    .asOf(executedAt)
                    .qtyOriginal(f.getQuantity())
                    .qtyRemaining(f.getQuantity())
                    .price(f.getPrice())
                    .build();
            lotRepo.save(lot);

            cashService.recordCashFlow(f.getPortfolioId(), total, tradeCcy, CashFlowType.TRADE_DEBIT, executedAt);

        } else {
            // SELL logic
            int oldQty = Optional.ofNullable(pos.getQuantity()).orElse(0);
            int available = oldQty - Optional.ofNullable(pos.getReservedQuantity()).orElse(0);

            if (f.getQuantity() > available) {
                throw new IllegalArgumentException("Cannot sell more than available quantity (short selling prevented)");
            }

            // Reserve sell
            pos.setReservedQuantity(pos.getReservedQuantity() + f.getQuantity());

            // Consume lots
            var lots = lotRepo.findByPosition_PositionIdOrderByAsOfAscForUpdate(pos.getPositionId());
            int remaining = f.getQuantity();
            for (var lot : lots) {
                if (remaining == 0) break;
                int c = Math.min(remaining, lot.getQtyRemaining());
                lot.setQtyRemaining(lot.getQtyRemaining() - c);
                remaining -= c;
            }
            lotRepo.saveAll(lots);

            // Update position
            pos.setQuantity(oldQty - f.getQuantity());
            pos.setReservedQuantity(pos.getReservedQuantity() - f.getQuantity());

            cashService.recordCashFlow(f.getPortfolioId(), total, tradeCcy, CashFlowType.TRADE_CREDIT, executedAt);
        }

        // Save Position with optimistic lock
        Position saved = positionRepo.save(pos);

        // Save TradeExecution
        CashFlow cf = cashService.recordCashFlow(f.getPortfolioId(), total, tradeCcy,
                f.getSide() == Side.BUY ? CashFlowType.TRADE_DEBIT : CashFlowType.TRADE_CREDIT, executedAt);

        TradeExecution te = TradeExecution.builder()
                .externalTradeId(f.getExternalTradeId())
                .externalOrderId(f.getExternalOrderId())
                .portfolio(portfolio)
                .stock(pos.getStock())
                .executedAt(executedAt)
                .price(f.getPrice())
                .quantity(f.getQuantity())
                .side(f.getSide())
                .currency(tradeCcy)
                .cashImpactBase(cashImpactBase)
                .cashFlowId(cf.getId())
                .build();
        tradeExecutionRepo.save(te);

        var s = saved.getStock();
        return new PositionView(saved.getPositionId(), s.getId(), s.getSymbol(),
                saved.getQuantity(), saved.getAveragePrice());
    }

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
        List<PositionLot> lotsFifo = lotRepo.findByPosition_PositionIdOrderByAsOfAscForUpdate(pos.getPositionId());

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
        var lots = lotRepo.findByPosition_PositionIdOrderByAsOfAscForUpdate(positionId);
        List<LotView> out = new ArrayList<>();
        for (var l : lots) {
            out.add(new LotView(l.getId(), l.getAsOf(), l.getQtyOriginal(), l.getQtyRemaining(), l.getPrice()));
        }
        return out;
    }
}
