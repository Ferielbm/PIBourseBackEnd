package tn.esprit.piboursebackend.Order.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderSide;
import tn.esprit.piboursebackend.Order.Entity.OrderStatus;
import tn.esprit.piboursebackend.Order.Entity.OrderType;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;
import tn.esprit.piboursebackend.Portfolio.Dto.FillRequest;
import tn.esprit.piboursebackend.Portfolio.Dto.Side;
import tn.esprit.piboursebackend.Portfolio.service.PositionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final PositionService positionService;

    public OrderService(OrderRepository orderRepo, PositionService positionService) {
        this.orderRepo = orderRepo;
        this.positionService = positionService;
    }

    /**
     * Submit a new order and attempt matching immediately.
     */
    @Transactional
    public Order submitOrder(Long portfolioId, Stock stock, BigDecimal qty, BigDecimal price, OrderSide side, OrderType type) {
        Order o = Order.builder()
                .stock(stock)
                .quantity(qty)
                .remainingQuantity(qty)
                .side(side)
                .type(type)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .build();

        o = orderRepo.save(o);
        matchOrder(o, portfolioId);
        return o;
    }

    /**
     * Match an order with opposite side orders, supports partial fills and queuing.
     */
    @Transactional
    public void matchOrder(Order incoming, Long portfolioId) {
        List<Order> counterOrders = incoming.getSide() == OrderSide.BUY
                ? orderRepo.findAsksForMatching(incoming.getStock())
                : orderRepo.findBidsForMatching(incoming.getStock());

        BigDecimal remaining = incoming.getRemainingQuantity();

        for (Order counter : counterOrders) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            // Check if prices match (for limit orders)
            if (incoming.getType() == OrderType.LIMIT && counter.getPrice() != null) {
                if (incoming.getSide() == OrderSide.BUY && incoming.getPrice().compareTo(counter.getPrice()) < 0) continue;
                if (incoming.getSide() == OrderSide.SELL && incoming.getPrice().compareTo(counter.getPrice()) > 0) continue;
            }

            BigDecimal tradeQty = remaining.min(counter.getRemainingQuantity());
            BigDecimal tradePrice = (incoming.getType() == OrderType.MARKET || counter.getPrice() == null)
                    ? counter.getPrice()
                    : incoming.getPrice();

            // Prepare FillRequests
            FillRequest fBuy = incoming.getSide() == OrderSide.BUY
                    ? new FillRequest(portfolioId, incoming.getStock().getId(), Side.BUY, tradeQty.intValue(), tradePrice, Instant.now(), "AUTO-" + Instant.now().toEpochMilli(), null, null)
                    : new FillRequest(counter.getId(), counter.getStock().getId(), Side.BUY, tradeQty.intValue(), tradePrice, Instant.now(), "AUTO-" + Instant.now().toEpochMilli(), null, null);

            FillRequest fSell = incoming.getSide() == OrderSide.SELL
                    ? new FillRequest(portfolioId, incoming.getStock().getId(), Side.SELL, tradeQty.intValue(), tradePrice, Instant.now(), "AUTO-" + Instant.now().toEpochMilli(), null, null)
                    : new FillRequest(counter.getId(), counter.getStock().getId(), Side.SELL, tradeQty.intValue(), tradePrice, Instant.now(), "AUTO-" + Instant.now().toEpochMilli(), null, null);

            // Apply fills
            positionService.applyFillWithReservation(fBuy);
            positionService.applyFillWithReservation(fSell);

            // Update counter order
            counter.setRemainingQuantity(counter.getRemainingQuantity().subtract(tradeQty));
            counter.setStatus(counter.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0
                    ? OrderStatus.FILLED
                    : OrderStatus.PARTIALLY_FILLED);
            orderRepo.save(counter);

            remaining = remaining.subtract(tradeQty);
        }

        // Update incoming order
        incoming.setRemainingQuantity(remaining);
        incoming.setStatus(remaining.compareTo(BigDecimal.ZERO) == 0
                ? OrderStatus.FILLED
                : (remaining.compareTo(incoming.getQuantity()) < 0 ? OrderStatus.PARTIALLY_FILLED : OrderStatus.PENDING));
        orderRepo.save(incoming);
    }
}
