package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Anomaly;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {
    List<Anomaly> findByStock_Symbol(String symbol);
    List<Anomaly> findByResolvedFalse();
    List<Anomaly> findByType(String type);

    @Query("SELECT a FROM Anomaly a WHERE a.detectedAt BETWEEN :startDate AND :endDate")
    List<Anomaly> findByDetectedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Anomaly a WHERE a.severity > :minSeverity ORDER BY a.severity DESC")
    List<Anomaly> findBySeverityGreaterThan(@Param("minSeverity") BigDecimal minSeverity);

    @Query("SELECT COUNT(a) FROM Anomaly a WHERE a.resolved = false")
    Long countUnresolvedAnomalies();

    @Query("SELECT a.type, COUNT(a) FROM Anomaly a GROUP BY a.type")
    List<Object[]> countAnomaliesByType();
}