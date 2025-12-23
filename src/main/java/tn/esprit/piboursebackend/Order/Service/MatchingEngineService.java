package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Repository.PriceHistoryRepository;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;
import tn.esprit.piboursebackend.Order.Repository.TradeRepository;
import tn.esprit.piboursebackend.Order.dto.OrderBookStats;
import tn.esprit.piboursebackend.Order.Entity.BookSnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingEngineService {

    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;
    private final OrderBookService orderBookService;
    private final TradeRepository tradeRepo;
    private final WalletService walletService;
    private final PriceAlertMonitorService priceAlertMonitorService;
    private final PriceHistoryRepository priceHistoryRepo;

    /* -------------------------------------------
       Hooks pour le carnet d’ordres / WebSocket
       ------------------------------------------- */

    public void onLimitOrderPlaced(Long playerId, String symbol, boolean isBid, double price, long qty) {
        // Recalcule la profondeur à ce niveau depuis la BDD (agrégation correcte)
        orderBookService.recomputeLevelFromDb(symbol, isBid, price);
        orderBookService.publishSnapshot(playerId, symbol);
    }

    public void onTradeExecuted(Long playerIdBuyer, Long playerIdSeller, String symbol, double executedPrice) {
        orderBookService.setLastPrice(symbol, executedPrice);
        orderBookService.publishSnapshot(playerIdBuyer, symbol);
        orderBookService.publishSnapshot(playerIdSeller, symbol);
    }

    /* -------------------------------------------
       Stats simples du carnet (Bid/Ask/Last)
       ------------------------------------------- */

    public OrderBookStats getStatsForSymbol(String symbol) {
        BookSnapshot snap = orderBookService.getSnapshot(symbol);

        BigDecimal bestBid = null;
        BigDecimal bestAsk = null;
        BigDecimal last = null;

        try {
            if (snap.getBids() != null && !snap.getBids().isEmpty()) {
                // keys are formatted strings; pick max for bids
                double maxBid = snap.getBids().keySet().stream()
                        .mapToDouble(k -> {
                            try { return Double.parseDouble(k); } catch (Exception e) { return Double.NEGATIVE_INFINITY; }
                        })
                        .max().orElse(Double.NaN);
                if (!Double.isNaN(maxBid)) bestBid = BigDecimal.valueOf(maxBid);
            }
            if (snap.getAsks() != null && !snap.getAsks().isEmpty()) {
                // keys are formatted strings; pick min for asks
                double minAsk = snap.getAsks().keySet().stream()
                        .mapToDouble(k -> {
                            try { return Double.parseDouble(k); } catch (Exception e) { return Double.POSITIVE_INFINITY; }
                        })
                        .min().orElse(Double.NaN);
                if (!Double.isNaN(minAsk)) bestAsk = BigDecimal.valueOf(minAsk);
            }
            if (snap.getLastPrice() != null) {
                last = BigDecimal.valueOf(snap.getLastPrice());
            }
        } catch (Exception ignored) {}

        return new OrderBookStats(symbol, bestBid, bestAsk, last);
    }

    /* -------------------------------------------
       Place Order + Matching immédiat
       ------------------------------------------- */

    @Transactional
    public Order placeOrder(
            Long playerId,
            String symbol,
            OrderSide side,
            OrderType type,
            TimeInForce tif,
            BigDecimal quantity,
            BigDecimal price
    ) {
        Stock stock = stockRepo.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));
        
        // Synchroniser le currentPrice du stock avec le dernier prix de price_history
        priceHistoryRepo.findLatestBySymbol(symbol).ifPresent(priceHistory -> {
            stock.setCurrentPrice(priceHistory.getClosePrice());
        });

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        if (type == OrderType.LIMIT && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Price must be > 0 for LIMIT orders");
        }

        Order order = new Order();
        order.setPlayerId(playerId);
        order.setStock(stock);
        order.setSide(side);
        order.setType(type);
        order.setTif(tif != null ? tif : TimeInForce.DAY);
        order.setStatus(OrderStatus.PENDING);
        order.setQuantity(quantity);
        order.setRemainingQuantity(quantity);
        order.setPrice(type == OrderType.MARKET ? null : price);

        order = orderRepo.save(order);

        // Réservation de cash pour BUY LIMIT
        if (order.getSide() == OrderSide.BUY && order.getType() == OrderType.LIMIT) {
            BigDecimal amount = order.getPrice().multiply(order.getQuantity());
            walletService.reserve(playerId, order.getId(), amount, "ORDER_RESERVE");
        }

        // Mettre à jour le carnet immédiatement pour un LIMIT (même si aucun match n'a lieu)
        if (order.getType() == OrderType.LIMIT && order.getPrice() != null) {
            boolean isBid = order.getSide() == OrderSide.BUY;
            onLimitOrderPlaced(
                    playerId,
                    stock.getSymbol(),
                    isBid,
                    order.getPrice().doubleValue(),
                    order.getRemainingQuantity().longValue()
            );
        }

        // Matching immédiat sur cet ordre
        match(order);

        return order;
    }

    /* -------------------------------------------
       Matching d’un ordre donné
       ------------------------------------------- */

    @Transactional
    protected void match(Order order) {
        if (order.getRemainingQuantity() == null ||
                order.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Stock stock = order.getStock();

        // Opposite side, status PENDING ou PARTIALLY_FILLED
        List<Order> candidates = (order.getSide() == OrderSide.BUY)
                ? orderRepo.findAsksForMatching(stock)
                : orderRepo.findBidsForMatching(stock);

        for (Order candidate : candidates) {
            if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) break;

            if (candidate.getId().equals(order.getId())) continue;
            if (candidate.getPlayerId().equals(order.getPlayerId())) continue; // pas d’auto-trade

            BigDecimal candidatePrice = candidate.getPrice();
            BigDecimal incomingPrice = order.getPrice();

            boolean pricesMatch = false;

            // Market -> toujours match
            if (order.getType() == OrderType.MARKET || candidate.getType() == OrderType.MARKET) {
                pricesMatch = true;
            } else {
                if (order.getSide() == OrderSide.BUY) {
                    // BUY LIMIT match si ask.price <= buy.price
                    if (candidatePrice != null && incomingPrice != null &&
                            candidatePrice.compareTo(incomingPrice) <= 0) {
                        pricesMatch = true;
                    }
                } else {
                    // SELL LIMIT match si bid.price >= sell.price
                    if (candidatePrice != null && incomingPrice != null &&
                            candidatePrice.compareTo(incomingPrice) >= 0) {
                        pricesMatch = true;
                    }
                }
            }

            if (!pricesMatch) continue;

            BigDecimal executedPrice = (candidatePrice != null) ? candidatePrice : incomingPrice;
            if (executedPrice == null) continue;

            BigDecimal qtyToTrade = order.getRemainingQuantity()
                    .min(candidate.getRemainingQuantity());

            if (qtyToTrade.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Création trade
            Trade trade = Trade.builder()
                    .stock(stock)
                    .buyOrder(order.getSide() == OrderSide.BUY ? order : candidate)
                    .sellOrder(order.getSide() == OrderSide.SELL ? order : candidate)
                    .price(executedPrice)
                    .quantity(qtyToTrade)
                    .build();

            trade = tradeRepo.save(trade);

            BigDecimal tradeAmount = trade.getPrice().multiply(trade.getQuantity());
            Long buyOrderId = trade.getBuyOrder().getId();
            Long buyerId = trade.getBuyOrder().getPlayerId();
            Long sellerId = trade.getSellOrder().getPlayerId();

            // Consommer la réservation et transférer au vendeur
            // ⚠️ Vérifier si l'ordre BUY a une réservation (LIMIT orders only)
            Order buyOrder = trade.getBuyOrder();
            if (buyOrder.getType() == OrderType.LIMIT) {
                walletService.consume(buyOrderId, tradeAmount);
            }
            walletService.transfer(buyerId, sellerId, tradeAmount);

            // MAJ des quantités et statuts
            order.setRemainingQuantity(order.getRemainingQuantity().subtract(qtyToTrade));
            candidate.setRemainingQuantity(candidate.getRemainingQuantity().subtract(qtyToTrade));

            if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                order.setStatus(OrderStatus.FILLED);
            } else {
                order.setStatus(OrderStatus.PARTIALLY_FILLED);
            }

            if (candidate.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                candidate.setStatus(OrderStatus.FILLED);
            } else {
                candidate.setStatus(OrderStatus.PARTIALLY_FILLED);
            }

            orderRepo.save(candidate);
            orderRepo.save(order);

            // MAJ carnet par recalcul DB (candidats + ordre entrant)
            if (candidate.getPrice() != null) {
                boolean isBid = candidate.getSide() == OrderSide.BUY;
                orderBookService.recomputeLevelFromDb(
                        stock.getSymbol(),
                        isBid,
                        candidate.getPrice().doubleValue()
                );
            }

            if (order.getPrice() != null) {
                boolean isBidIncoming = order.getSide() == OrderSide.BUY;
                orderBookService.recomputeLevelFromDb(
                        stock.getSymbol(),
                        isBidIncoming,
                        order.getPrice().doubleValue()
                );
            }

            onTradeExecuted(buyerId, sellerId, stock.getSymbol(), executedPrice.doubleValue());
            
            // Vérifier les alertes de prix après chaque transaction
            log.info("🔔 Vérification des alertes pour {} @ {}", stock.getSymbol(), executedPrice);
            priceAlertMonitorService.checkAlertsForSymbol(stock.getSymbol(), executedPrice);
            log.info("✅ Vérification des alertes terminée");
        }
    }

    /* -------------------------------------------
       Matching par symbole (utilisé par le scheduler)
       ------------------------------------------- */

    @Transactional
    public void matchSymbol(String symbol) {
        Stock stock = stockRepo.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));

        // Backfill des réservations pour BUY LIMIT
        List<Order> buyOrders = orderRepo
                .findByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(
                        stock,
                        OrderSide.BUY,
                        List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED)
                );

        for (Order b : buyOrders) {
            if (b.getType() == OrderType.LIMIT) {
                try {
                    BigDecimal amount = b.getPrice().multiply(b.getRemainingQuantity());
                    walletService.reserve(b.getPlayerId(), b.getId(), amount, "BACKFILL_RESERVE");
                } catch (Exception ex) {
                    // si échec de réservation -> on ignore
                }
            }
        }

        List<OrderStatus> open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);

        // Bids desc
        List<Order> orders = orderRepo
                .findByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(stock, OrderSide.BUY, open);
        // Asks asc
        orders.addAll(
                orderRepo.findByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(stock, OrderSide.SELL, open)
        );

        for (Order o : orders) {
            orderRepo.findById(o.getId()).ifPresent(this::match);
        }
    }

    /* -------------------------------------------
       Scheduler global : scan des symboles
       ------------------------------------------- */

    @Scheduled(fixedDelayString = "${matching.scan.delay:5000}")
    public void scheduledMatchScan() {
        List<OrderStatus> open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);

        // On récupère tous les ordres ouverts
        List<Order> openOrders = orderRepo.findByStatusIn(open);

        // On en déduit les symboles uniques
        Set<String> symbols = openOrders.stream()
                .filter(o -> o.getStock() != null)
                .map(o -> o.getStock().getSymbol())
                .collect(Collectors.toSet());

        for (String symbol : symbols) {
            try {
                matchSymbol(symbol);
            } catch (Exception ex) {
                // en prod -> logger
            }
        }
    }

    /* -------------------------------------------
       Annulation d’un ordre
       ------------------------------------------- */

    @Transactional
    public void cancelOpenOrder(Long playerId, Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!playerId.equals(order.getPlayerId())) {
            throw new IllegalArgumentException("Order " + orderId + " does not belong to player " + playerId);
        }

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.PARTIALLY_FILLED) {
            throw new IllegalArgumentException("Order " + orderId + " is not open and cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
    }
}
