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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepo;
    private final TradeRepository tradeRepo;
    private final StockRepository stockRepo;

    @Transactional(readOnly = true)
    public BookSnapshot getBook(String symbol) {
        Stock stock = stockRepo.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));

        var open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);

        var bids = orderRepo.findTop50ByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(
                stock, OrderSide.BUY, open);
        var asks = orderRepo.findTop50ByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(
                stock, OrderSide.SELL, open);

        BigDecimal last = tradeRepo.findTop50ByStockOrderByExecutedAtDesc(stock).stream()
                .findFirst().map(Trade::getPrice).orElse(null);

        return new BookSnapshot(symbol, aggregate(bids), aggregate(asks), last);
    }

    private static Map<BigDecimal, BigDecimal> aggregate(List<Order> orders) {
        Map<BigDecimal, BigDecimal> levels = new LinkedHashMap<>();
        for (Order o : orders) {
            if (o.getPrice() == null) continue; // on n’agrège que des LIMIT
            levels.merge(o.getPrice(), o.getRemainingQuantity(), BigDecimal::add);
        }
        return levels;
    }

    public record BookSnapshot(
            String symbol,
            Map<BigDecimal, BigDecimal> bids,
            Map<BigDecimal, BigDecimal> asks,
            BigDecimal lastPrice
    ) {}
}
