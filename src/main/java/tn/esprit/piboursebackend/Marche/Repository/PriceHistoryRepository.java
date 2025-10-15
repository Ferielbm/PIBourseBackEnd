package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByStock_SymbolOrderByDateTime(String symbol);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.stock.symbol = :symbol AND ph.dateTime BETWEEN :startDate AND :endDate ORDER BY ph.dateTime")
    List<PriceHistory> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.stock.symbol = :symbol AND ph.dateTime >= :date ORDER BY ph.dateTime")
    List<PriceHistory> findByStock_SymbolAndDateTimeAfter(@Param("symbol") String symbol,
                                                          @Param("date") LocalDateTime date);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.stock.symbol = :symbol AND DATE(ph.dateTime) = DATE(:date)")
    Optional<PriceHistory> findBySymbolAndDate(@Param("symbol") String symbol,
                                               @Param("date") LocalDateTime date);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.stock.symbol = :symbol ORDER BY ph.dateTime DESC LIMIT 1")
    Optional<PriceHistory> findLatestBySymbol(@Param("symbol") String symbol);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.stock.symbol = :symbol AND ph.dateTime = (SELECT MAX(ph2.dateTime) FROM PriceHistory ph2 WHERE ph2.stock.symbol = :symbol)")
    Optional<PriceHistory> findMostRecentBySymbol(@Param("symbol") String symbol);
}