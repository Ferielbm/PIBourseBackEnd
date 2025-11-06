package tn.esprit.piboursebackend.GameSession.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.GameSession.Entities.SessionPlayer;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionPlayerRepository extends JpaRepository<SessionPlayer, Long> {
    
    /**
     * Find all players in a session
     */
    List<SessionPlayer> findByGameSessionId(Long sessionId);
    
    /**
     * Find all active players in a session
     */
    List<SessionPlayer> findByGameSessionIdAndIsActiveTrue(Long sessionId);
    
    /**
     * Find all sessions a player has joined
     */
    List<SessionPlayer> findByPlayerId(Long playerId);
    
    /**
     * Find a specific player in a specific session
     */
    Optional<SessionPlayer> findByGameSessionIdAndPlayerId(Long sessionId, Long playerId);
    
    /**
     * Check if a player exists in a session
     */
    boolean existsByGameSessionIdAndPlayerId(Long sessionId, Long playerId);
    
    /**
     * Get leaderboard (ordered by total value descending)
     */
    @Query("SELECT sp FROM SessionPlayer sp WHERE sp.gameSession.id = :sessionId " +
           "AND sp.isActive = true ORDER BY sp.totalValue DESC")
    List<SessionPlayer> findLeaderboard(@Param("sessionId") Long sessionId);
    
    /**
     * Get leaderboard by profit/loss percentage
     */
    @Query("SELECT sp FROM SessionPlayer sp WHERE sp.gameSession.id = :sessionId " +
           "AND sp.isActive = true ORDER BY sp.profitLossPercentage DESC NULLS LAST")
    List<SessionPlayer> findLeaderboardByPerformance(@Param("sessionId") Long sessionId);
    
    /**
     * Count active players in a session
     */
    long countByGameSessionIdAndIsActiveTrue(Long sessionId);
    
    /**
     * Find top N performers in a session
     */
    @Query("SELECT sp FROM SessionPlayer sp WHERE sp.gameSession.id = :sessionId " +
           "AND sp.isActive = true ORDER BY sp.totalValue DESC")
    List<SessionPlayer> findTopPerformers(@Param("sessionId") Long sessionId);
    
    /**
     * Get player statistics across all sessions
     */
    @Query("SELECT sp FROM SessionPlayer sp WHERE sp.player.id = :playerId " +
           "ORDER BY sp.gameSession.createdAt DESC")
    List<SessionPlayer> findPlayerHistory(@Param("playerId") Long playerId);
    
    /**
     * Find inactive players in a session
     */
    @Query("SELECT sp FROM SessionPlayer sp WHERE sp.gameSession.id = :sessionId " +
           "AND sp.isActive = false")
    List<SessionPlayer> findInactivePlayers(@Param("sessionId") Long sessionId);
    
    /**
     * Delete all players from a session
     */
    void deleteByGameSessionId(Long sessionId);
}

