package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderSide;
import tn.esprit.piboursebackend.Order.Entity.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findTop50ByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(
            Stock stock, OrderSide side, List<OrderStatus> statuses);

    List<Order> findTop50ByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(
            Stock stock, OrderSide side, List<OrderStatus> statuses);

    // Makers côté ASK pour matcher contre un BUY
    @Query("""
      SELECT o FROM Order o
      WHERE o.stock = :stock
        AND o.side = tn.esprit.piboursebackend.Order.Entity.OrderSide.SELL
        AND o.status IN (tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING,
                         tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
        AND o.type = tn.esprit.piboursebackend.Order.Entity.OrderType.LIMIT
        AND o.price IS NOT NULL
      ORDER BY o.price ASC, o.createdAt ASC
    """)
    List<Order> findAsksForMatching(Stock stock);

    // Makers côté BID pour matcher contre un SELL
    @Query("""
      SELECT o FROM Order o
      WHERE o.stock = :stock
        AND o.side = tn.esprit.piboursebackend.Order.Entity.OrderSide.BUY
        AND o.status IN (tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING,
                         tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
        AND o.type = tn.esprit.piboursebackend.Order.Entity.OrderType.LIMIT
        AND o.price IS NOT NULL
      ORDER BY o.price DESC, o.createdAt ASC
    """)
    List<Order> findBidsForMatching(Stock stock);
}
