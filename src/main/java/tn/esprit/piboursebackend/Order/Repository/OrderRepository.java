package tn.esprit.piboursebackend.Order.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderSide;
import tn.esprit.piboursebackend.Order.Entity.OrderStatus;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    // Taker = BUY -> on prend les SELL (asks) au meilleur prix puis FIFO
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      SELECT o FROM Order o
       WHERE o.stock = :stock
         AND o.side = tn.esprit.piboursebackend.Order.Entity.OrderSide.SELL
         AND (o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING
              OR o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
         AND o.price IS NOT NULL
       ORDER BY o.price ASC, o.createdAt ASC
    """)
    List<Order> findAsksForMatching(@Param("stock") Stock stock);

    // Taker = SELL -> on prend les BUY (bids) au meilleur prix puis FIFO
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      SELECT o FROM Order o
       WHERE o.stock = :stock
         AND o.side = tn.esprit.piboursebackend.Order.Entity.OrderSide.BUY
         AND (o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING
              OR o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
         AND o.price IS NOT NULL
       ORDER BY o.price DESC, o.createdAt ASC
    """)
    List<Order> findBidsForMatching(@Param("stock") Stock stock);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      SELECT o FROM Order o
        JOIN o.stock s
       WHERE UPPER(s.symbol) = UPPER(:symbol)
         AND o.side = tn.esprit.piboursebackend.Order.Entity.OrderSide.SELL
         AND (o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING
              OR o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
         AND o.price IS NOT NULL
       ORDER BY o.price ASC, o.createdAt ASC
    """)
    List<Order> findAsksForMatchingBySymbol(@Param("symbol") String symbol);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      SELECT o FROM Order o
        JOIN o.stock s
       WHERE UPPER(s.symbol) = UPPER(:symbol)
         AND o.side = tn.esprit.piboursebackend.Order.Entity.OrderSide.BUY
         AND (o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING
              OR o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
         AND o.price IS NOT NULL
       ORDER BY o.price DESC, o.createdAt ASC
    """)
    List<Order> findBidsForMatchingBySymbol(@Param("symbol") String symbol);


    List<Order> findTop50ByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(
            Stock stock, OrderSide side, Collection<OrderStatus> statuses);

    List<Order> findTop50ByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(
            Stock stock, OrderSide side, Collection<OrderStatus> statuses);


    @Query("""
      SELECT COUNT(o) FROM Order o
       WHERE o.stock = :stock
         AND (o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PENDING
              OR o.status = tn.esprit.piboursebackend.Order.Entity.OrderStatus.PARTIALLY_FILLED)
    """)
    long countOpenByStock(@Param("stock") Stock stock);
}
