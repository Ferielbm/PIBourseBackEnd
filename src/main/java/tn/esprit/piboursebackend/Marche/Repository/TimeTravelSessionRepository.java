package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.TimeTravelSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeTravelSessionRepository extends JpaRepository<TimeTravelSession, Long> {

    Optional<TimeTravelSession> findBySessionId(String sessionId);

    List<TimeTravelSession> findByPlayerIdOrderByCreatedAtDesc(String playerId);

    List<TimeTravelSession> findByPlayerIdAndStatus(String playerId, TimeTravelSession.TimeTravelStatus status);

    @Query("SELECT t FROM TimeTravelSession t WHERE t.playerId = :playerId AND t.rewindToDate BETWEEN :startDate AND :endDate")
    List<TimeTravelSession> findSessionsInDateRange(@Param("playerId") String playerId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM TimeTravelSession t WHERE t.playerId = :playerId AND t.status = 'ACTIVE'")
    long countActiveSessionsByPlayer(@Param("playerId") String playerId);
}