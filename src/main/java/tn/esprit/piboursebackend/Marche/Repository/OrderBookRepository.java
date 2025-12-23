package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.OrderBook;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {
    Optional<OrderBook> findByStock_Symbol(String symbol);

    @Query("SELECT ob FROM OrderBook ob WHERE ob.liquidity > :minLiquidity ORDER BY ob.liquidity DESC")
    List<OrderBook> findByLiquidityGreaterThan(@Param("minLiquidity") BigDecimal minLiquidity);

    @Query("SELECT AVG(ob.spread) FROM OrderBook ob WHERE ob.spread IS NOT NULL")
    Optional<Double> findAverageSpread();

    @Query("SELECT ob FROM OrderBook ob WHERE ob.stock.sector = :sector")
    List<OrderBook> findByStockSector(@Param("sector") String sector);
}