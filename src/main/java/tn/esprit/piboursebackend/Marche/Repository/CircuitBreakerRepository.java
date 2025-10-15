package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.CircuitBreaker;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CircuitBreakerRepository extends JpaRepository<CircuitBreaker, Long> {
    List<CircuitBreaker> findByStock_Symbol(String symbol);
    List<CircuitBreaker> findByActiveTrue();

    @Query("SELECT cb FROM CircuitBreaker cb WHERE cb.triggeredAt BETWEEN :startDate AND :endDate")
    List<CircuitBreaker> findByTriggeredAtBetween(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT cb FROM CircuitBreaker cb WHERE cb.stock.symbol = :symbol AND cb.active = true")
    List<CircuitBreaker> findActiveByStockSymbol(@Param("symbol") String symbol);

    @Query("SELECT COUNT(cb) FROM CircuitBreaker cb WHERE cb.active = true")
    Long countActiveCircuitBreakers();
}