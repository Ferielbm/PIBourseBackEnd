package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderSide;
import tn.esprit.piboursebackend.Order.Entity.OrderStatus;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Ordres d’un joueur
    List<Order> findByPlayerIdOrderByIdDesc(Long playerId);

    // Pour matching & carnet d’ordres
    List<Order> findByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(
            Stock stock,
            OrderSide side,
            Collection<OrderStatus> status
    );
    @Query("""
           SELECT o FROM Order o
           WHERE o.stock.symbol = :symbol
             AND o.side = :side
             AND o.status = :status
             AND o.price BETWEEN :minPrice AND :maxPrice
           ORDER BY o.price ASC
           """)
    List<Order> findMatchingOrdersInRange(
            @Param("symbol") String symbol,
            @Param("side") OrderSide side,
            @Param("status") OrderStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
    List<Order> findByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(
            Stock stock,
            OrderSide side,
            Collection<OrderStatus> status
    );

    List<Order> findTop50ByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(
            Stock stock,
            OrderSide side,
            Collection<OrderStatus> status
    );

    List<Order> findTop50ByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(
            Stock stock,
            OrderSide side,
            Collection<OrderStatus> status
    );

    // Utilisé par le scheduler pour récupérer tous les ordres ouverts
    List<Order> findByStatusIn(Collection<OrderStatus> status);

    default List<Order> findBidsForMatching(Stock stock) {
        var open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
        return findByStockAndSideAndStatusInOrderByPriceDescCreatedAtAsc(stock, OrderSide.BUY, open);
    }

    default List<Order> findAsksForMatching(Stock stock) {
        var open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
        return findByStockAndSideAndStatusInOrderByPriceAscCreatedAtAsc(stock, OrderSide.SELL, open);
    }

    // Méthodes pour GameMaster
    List<Order> findByStockAndStatus(Stock stock, OrderStatus status);
    
    List<Order> findByStatus(OrderStatus status);
    
    long countByStatus(OrderStatus status);
    
    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM Order o WHERE o.status = :status")
    Long sumQuantityByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(DISTINCT o.playerId) FROM Order o WHERE o.status = :status")
    int countDistinctPlayersByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT o.stock.symbol FROM Order o WHERE o.status = :status GROUP BY o.stock.symbol ORDER BY COUNT(o) DESC")
    String findMostTradedSymbol();
    
    // Charger tous les ordres avec les relations stock
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.stock ORDER BY o.createdAt DESC")
    List<Order> findAllWithStock();

                /**
                 * Somme de remainingQuantity pour un niveau de prix, un symbole et un side, sur des statuts ouverts
                 */
                @Query("""
                                         SELECT COALESCE(SUM(o.remainingQuantity), 0)
                                         FROM Order o
                                         WHERE o.stock.symbol = :symbol
                                                 AND o.side = :side
                                                 AND o.status IN :statuses
                                                 AND o.price = :price
                                         """)
                BigDecimal sumRemainingByLevel(
                                                @Param("symbol") String symbol,
                                                @Param("side") OrderSide side,
                                                @Param("statuses") Collection<OrderStatus> statuses,
                                                @Param("price") BigDecimal price
                );


}

