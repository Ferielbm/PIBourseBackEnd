// tn/esprit/piboursebackend/Order/Service/OrderBookService.java
package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Order.Entity.BookSnapshot;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderDTO;
import tn.esprit.piboursebackend.Order.Entity.OrderSide;
import tn.esprit.piboursebackend.Order.Entity.OrderStatus;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderRepository orderRepository;

    private final Map<String, NavigableMap<Double, Long>> bids = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<Double, Long>> asks = new ConcurrentHashMap<>();
    private final Map<String, Double> lastPrice = new ConcurrentHashMap<>();

    public BookSnapshot getSnapshot(String symbol) {
        String sym = symbol.toUpperCase(Locale.ROOT);
        NavigableMap<Double, Long> b = bids.computeIfAbsent(sym, k -> new TreeMap<>(Comparator.reverseOrder()));
        NavigableMap<Double, Long> a = asks.computeIfAbsent(sym, k -> new TreeMap<>());
        Double last = lastPrice.getOrDefault(sym, null);

        Map<String, Long> bMap = new LinkedHashMap<>();
        b.forEach((p, v) -> bMap.put(format(p), v));

        Map<String, Long> aMap = new LinkedHashMap<>();
        a.forEach((p, v) -> aMap.put(format(p), v));

        return BookSnapshot.builder()
                .bids(bMap)
                .asks(aMap)
                .lastPrice(last)
                .build();
    }

    public void upsertLevel(String symbol, boolean isBid, double price, long qty) {
        String sym = symbol.toUpperCase(Locale.ROOT);
        NavigableMap<Double, Long> side = (isBid
                ? bids.computeIfAbsent(sym, k -> new TreeMap<>(Comparator.reverseOrder()))
                : asks.computeIfAbsent(sym, k -> new TreeMap<>()));

        if (qty <= 0) {
            side.remove(price);
        } else {
            side.put(price, qty);
        }
    }

    /**
     * Recalcule la quantité du niveau depuis la base de données
     * pour garantir la cohérence en présence de plusieurs ordres au même prix.
     */
    public void recomputeLevelFromDb(String symbol, boolean isBid, double price) {
        String sym = symbol.toUpperCase(Locale.ROOT);
        NavigableMap<Double, Long> sideMap = (isBid
                ? bids.computeIfAbsent(sym, k -> new TreeMap<>(Comparator.reverseOrder()))
                : asks.computeIfAbsent(sym, k -> new TreeMap<>()));

        List<OrderStatus> open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
        BigDecimal total = orderRepository.sumRemainingByLevel(
                sym,
                isBid ? OrderSide.BUY : OrderSide.SELL,
                open,
                BigDecimal.valueOf(price)
        );

        long qty = (total == null) ? 0L : total.longValue();
        if (qty <= 0) {
            sideMap.remove(price);
        } else {
            sideMap.put(price, qty);
        }
    }

    public void setLastPrice(String symbol, double price) {
        lastPrice.put(symbol.toUpperCase(Locale.ROOT), price);
    }

    /** Push temps réel vers /topic/players/{playerId}/orderbook/{symbol} */
    public void publishSnapshot(Long playerId, String symbol) {
        BookSnapshot snap = getSnapshot(symbol);
        String destination = String.format("/topic/players/%d/orderbook/%s",
                playerId, symbol.toUpperCase(Locale.ROOT));
        messagingTemplate.convertAndSend(destination, snap);
    }

    private static String format(Double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }
    
    /**
     * Charger tous les ordres avec leurs relations pour l'admin
     */
    public List<OrderDTO> getAllOrdersWithRelations() {
        List<Order> orders = orderRepository.findAllWithStock();
        
        return orders.stream().map(order -> {
            OrderDTO dto = OrderDTO.builder()
                    .id(order.getId())
                    .playerId(order.getPlayerId())
                    .stockId(order.getStock() != null ? (long) order.getStock().getSymbol().hashCode() : null)
                    .type(order.getType())
                    .side(order.getSide())
                    .tif(order.getTif())
                    .status(order.getStatus())
                    .price(order.getPrice())
                    .quantity(order.getQuantity())
                    .remainingQuantity(order.getRemainingQuantity())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build();
            
            // Ajouter les infos du stock
            if (order.getStock() != null) {
                dto.setStock(OrderDTO.StockInfo.builder()
                        .id((long) order.getStock().getSymbol().hashCode())
                        .symbol(order.getStock().getSymbol())
                        .name(order.getStock().getCompanyName())
                        .lastPrice(order.getStock().getCurrentPrice())
                        .build());
            }
            
            // Ajouter les infos du player (si vous avez l'entité Player)
            dto.setPlayer(OrderDTO.PlayerInfo.builder()
                    .id(order.getPlayerId())
                    .username("Player " + order.getPlayerId())
                    .build());
            
            return dto;
        }).collect(Collectors.toList());
    }
}
