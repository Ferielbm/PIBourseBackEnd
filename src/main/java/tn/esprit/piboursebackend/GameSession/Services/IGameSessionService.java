package tn.esprit.piboursebackend.GameSession.Services;

import tn.esprit.piboursebackend.GameSession.DTOs.*;
import tn.esprit.piboursebackend.GameSession.Entities.SessionStatus;

import java.util.List;

public interface IGameSessionService {
    
    /**
     * Create a new game session
     */
    GameSessionDTO createSession(Long gameMasterId, CreateSessionRequest request);
    
    /**
     * Update an existing session (only if not started)
     */
    GameSessionDTO updateSession(Long sessionId, Long gameMasterId, UpdateSessionRequest request);
    
    /**
     * Get session by ID
     */
    GameSessionDTO getSessionById(Long sessionId);
    
    /**
     * Get all sessions created by a game master
     */
    List<GameSessionDTO> getSessionsByGameMaster(Long gameMasterId);
    
    /**
     * Get all sessions with a specific status
     */
    List<GameSessionDTO> getSessionsByStatus(SessionStatus status);
    
    /**
     * Get currently active sessions
     */
    List<GameSessionDTO> getActiveSessions();
    
    /**
     * Get upcoming sessions
     */
    List<GameSessionDTO> getUpcomingSessions();
    
    /**
     * Add a player to a session
     */
    SessionPlayerDTO addPlayerToSession(Long sessionId, Long gameMasterId, Long playerId);
    
    /**
     * Remove a player from a session (only before start)
     */
    void removePlayerFromSession(Long sessionId, Long gameMasterId, Long playerId);
    
    /**
     * Get all players in a session
     */
    List<SessionPlayerDTO> getSessionPlayers(Long sessionId);
    
    /**
     * Get session leaderboard
     */
    SessionLeaderboardDTO getSessionLeaderboard(Long sessionId);
    
    /**
     * Start a session
     */
    GameSessionDTO startSession(Long sessionId, Long gameMasterId);
    
    /**
     * Pause a session
     */
    GameSessionDTO pauseSession(Long sessionId, Long gameMasterId);
    
    /**
     * Resume a paused session
     */
    GameSessionDTO resumeSession(Long sessionId, Long gameMasterId);
    
    /**
     * Complete/End a session
     */
    GameSessionDTO completeSession(Long sessionId, Long gameMasterId);
    
    /**
     * Cancel a session
     */
    GameSessionDTO cancelSession(Long sessionId, Long gameMasterId);
    
    /**
     * Delete a session (only if not started)
     */
    void deleteSession(Long sessionId, Long gameMasterId);
    
    /**
     * Update player statistics (called after trades)
     */
    void updatePlayerStatistics(Long sessionId, Long playerId);
    
    /**
     * Calculate and update rankings for all players in a session
     */
    void updateSessionRankings(Long sessionId);
    
    /**
     * Get player performance in a specific session
     */
    SessionPlayerDTO getPlayerPerformance(Long sessionId, Long playerId);
    
    /**
     * Get all sessions a player has participated in
     */
    List<SessionPlayerDTO> getPlayerSessionHistory(Long playerId);
}

