package tn.esprit.piboursebackend.GameSession.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.GameSession.Entities.GameSession;
import tn.esprit.piboursebackend.GameSession.Entities.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    /**
     * Find all sessions created by a specific game master
     */
    List<GameSession> findByGameMasterId(Long gameMasterId);
    
    /**
     * Find sessions by status
     */
    List<GameSession> findByStatus(SessionStatus status);
    
    /**
     * Find active sessions
     */
    List<GameSession> findByStatusIn(List<SessionStatus> statuses);
    
    /**
     * Find sessions by game master and status
     */
    List<GameSession> findByGameMasterIdAndStatus(Long gameMasterId, SessionStatus status);
    
    /**
     * Find sessions that are currently active (between start and end dates)
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.status = 'ACTIVE' " +
           "AND gs.startDate <= :now AND gs.endDate >= :now")
    List<GameSession> findCurrentlyActiveSessions(@Param("now") LocalDateTime now);
    
    /**
     * Find upcoming sessions
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.status IN ('CREATED', 'READY') " +
           "AND gs.startDate > :now ORDER BY gs.startDate ASC")
    List<GameSession> findUpcomingSessions(@Param("now") LocalDateTime now);
    
    /**
     * Find completed sessions
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.status IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY gs.actualEndTime DESC")
    List<GameSession> findCompletedSessions();
    
    /**
     * Find sessions by name (case insensitive)
     */
    List<GameSession> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if a player is already in a session
     */
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END " +
           "FROM SessionPlayer sp WHERE sp.gameSession.id = :sessionId " +
           "AND sp.player.id = :playerId")
    boolean isPlayerInSession(@Param("sessionId") Long sessionId, 
                              @Param("playerId") Long playerId);
    
    /**
     * Count active sessions for a game master
     */
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.gameMaster.id = :gameMasterId " +
           "AND gs.status = 'ACTIVE'")
    long countActiveSessionsByGameMaster(@Param("gameMasterId") Long gameMasterId);
    
    /**
     * Find sessions ending soon (within next hours)
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.status = 'ACTIVE' " +
           "AND gs.endDate BETWEEN :now AND :endTime")
    List<GameSession> findSessionsEndingSoon(@Param("now") LocalDateTime now, 
                                             @Param("endTime") LocalDateTime endTime);
}

