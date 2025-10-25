package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;
import tn.esprit.piboursebackend.Order.Repository.TradeRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final OrderRepository orderRepo;
    private final TradeRepository tradeRepo;
    private final StockRepository stockRepo;
    private final AuditLogService audit;

    @Transactional
    public Order placeOrder(String actor,
                            String symbol,
                            OrderSide side,
                            OrderType type,
                            TimeInForce tif,
                            BigDecimal quantity,
                            BigDecimal limitPrice) {

        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("symbol is required");
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (type == OrderType.LIMIT && (limitPrice == null || limitPrice.signum() <= 0))
            throw new IllegalArgumentException("LIMIT order requires positive price");

        Stock stock = stockRepo.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));

        // MARKET => IOC par défaut ; sinon default = DAY
        if (type == OrderType.MARKET && (tif == null || tif == TimeInForce.DAY || tif == TimeInForce.GTC)) {
            tif = TimeInForce.IOC;
        }
        if (tif == null) tif = TimeInForce.DAY;

        Order taker = Order.builder()
                .stock(stock)
                .side(side)
                .type(type)
                .tif(tif)
                .quantity(quantity)
                .remainingQuantity(quantity)
                .price(type == OrderType.LIMIT ? limitPrice : null)
                .status(OrderStatus.PENDING)
                .build();
        taker = orderRepo.save(taker);

        // FOK : doit être totalement exécutable sinon rejet
        if (tif == TimeInForce.FOK) {
            BigDecimal canFill = estimateFillableQty(stock, taker);
            if (canFill.compareTo(quantity) < 0) {
                taker.setStatus(OrderStatus.REJECTED);
                orderRepo.save(taker);
                audit.log(actor, "ORDER_REJECTED_FOK", "orderId=" + taker.getId());
                return taker;
            }
        }

        matchLoop(stock, taker, actor);

        boolean filled = taker.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0;
        if (type == OrderType.MARKET || tif == TimeInForce.IOC) {
            if (filled) taker.setStatus(OrderStatus.FILLED);
            else taker.setStatus(
                    taker.getQuantity().compareTo(taker.getRemainingQuantity()) == 0
                            ? OrderStatus.REJECTED
                            : OrderStatus.CANCELLED);
        } else {
            if (filled) taker.setStatus(OrderStatus.FILLED);
            else if (taker.getRemainingQuantity().compareTo(taker.getQuantity()) < 0)
                taker.setStatus(OrderStatus.PARTIALLY_FILLED);
            else taker.setStatus(OrderStatus.PENDING);
        }
        taker = orderRepo.save(taker);
        audit.log(actor, "ORDER_PLACED", "orderId=" + taker.getId() + ", status=" + taker.getStatus());
        return taker;
    }

    @Transactional
    public void cancelOpenOrder(String actor, Long orderId) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!isOpen(o)) return;
        o.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(o);
        audit.log(actor, "ORDER_CANCELLED", "orderId=" + orderId);
    }

    /** Ne matche que contre des LIMIT avec prix non nul ; protège les nulls */
    private void matchLoop(Stock stock, Order taker, String actor) {
        boolean takerIsBuy = taker.getSide() == OrderSide.BUY;
        List<Order> book = takerIsBuy
                ? orderRepo.findAsksForMatching(stock)
                : orderRepo.findBidsForMatching(stock);

        for (Order maker : book) {
            if (!isOpen(taker)) break;
            if (!isOpen(maker)) continue;

            // Sécurité : ne prendre que LIMIT + prix non nul
            if (maker.getType() != OrderType.LIMIT || maker.getPrice() == null) continue;

            if (taker.getType() == OrderType.LIMIT) {
                if (taker.getPrice() == null) continue; // sécurité
                int cmp = maker.getPrice().compareTo(taker.getPrice());
                boolean cross = takerIsBuy ? (cmp <= 0) : (cmp >= 0);
                if (!cross) break;
            }

            BigDecimal execQty   = min(taker.getRemainingQuantity(), maker.getRemainingQuantity());
            BigDecimal execPrice = maker.getPrice(); // non null

            Trade trade = Trade.builder()
                    .stock(stock)
                    .buyOrder(takerIsBuy ? taker : maker)
                    .sellOrder(takerIsBuy ? maker : taker)
                    .price(execPrice)
                    .quantity(execQty)
                    .build();
            tradeRepo.save(trade);

            maker.setRemainingQuantity(maker.getRemainingQuantity().subtract(execQty));
            maker.setStatus(maker.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0
                    ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);
            orderRepo.save(maker);

            taker.setRemainingQuantity(taker.getRemainingQuantity().subtract(execQty));
            taker.setStatus(taker.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0
                    ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);
            orderRepo.save(taker);

            audit.log(actor, "TRADE_EXECUTED",
                    "buy=" + (takerIsBuy ? taker.getId() : maker.getId()) +
                            ", sell=" + (takerIsBuy ? maker.getId() : taker.getId()) +
                            ", price=" + execPrice + ", qty=" + execQty);
        }
    }

    private BigDecimal estimateFillableQty(Stock stock, Order taker) {
        boolean isBuy = taker.getSide() == OrderSide.BUY;
        List<Order> book = isBuy ? orderRepo.findAsksForMatching(stock)
                : orderRepo.findBidsForMatching(stock);
        BigDecimal need = taker.getRemainingQuantity();
        BigDecimal acc  = BigDecimal.ZERO;

        for (Order maker : book) {
            if (maker.getType() != OrderType.LIMIT || maker.getPrice() == null) continue;

            if (taker.getType() == OrderType.LIMIT) {
                if (taker.getPrice() == null) continue;
                int cmp = maker.getPrice().compareTo(taker.getPrice());
                boolean cross = isBuy ? (cmp <= 0) : (cmp >= 0);
                if (!cross) break;
            }
            BigDecimal rest = need.subtract(acc);
            BigDecimal take = rest.compareTo(maker.getRemainingQuantity()) <= 0 ? rest : maker.getRemainingQuantity();
            if (take.signum() > 0) acc = acc.add(take);
            if (acc.compareTo(need) >= 0) break;
        }
        return acc;
    }

    private static BigDecimal min(BigDecimal a, BigDecimal b){ return a.compareTo(b) <= 0 ? a : b; }
    private static boolean isOpen(Order o){
        return o.getStatus()==OrderStatus.PENDING || o.getStatus()==OrderStatus.PARTIALLY_FILLED;
    }
}
