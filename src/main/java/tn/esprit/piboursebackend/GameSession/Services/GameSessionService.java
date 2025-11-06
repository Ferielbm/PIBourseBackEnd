package tn.esprit.piboursebackend.GameSession.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.GameSession.DTOs.*;
import tn.esprit.piboursebackend.GameSession.Entities.GameSession;
import tn.esprit.piboursebackend.GameSession.Entities.SessionPlayer;
import tn.esprit.piboursebackend.GameSession.Entities.SessionStatus;
import tn.esprit.piboursebackend.GameSession.Repositories.GameSessionRepository;
import tn.esprit.piboursebackend.GameSession.Repositories.SessionPlayerRepository;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Exceptions.ResourceNotFoundException;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameSessionService implements IGameSessionService {

    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);

    private final GameSessionRepository gameSessionRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final PlayerRepository playerRepository;

    public GameSessionService(GameSessionRepository gameSessionRepository,
                              SessionPlayerRepository sessionPlayerRepository,
                              PlayerRepository playerRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.sessionPlayerRepository = sessionPlayerRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public GameSessionDTO createSession(Long gameMasterId, CreateSessionRequest request) {
        logger.info("Creating new game session for Game Master ID: {}", gameMasterId);

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        // Get game master
        Player gameMaster = playerRepository.findById(gameMasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Game Master non trouvé avec l'ID: " + gameMasterId));

        // Create session
        GameSession session = GameSession.builder()
                .name(request.getName())
                .description(request.getDescription())
                .gameMaster(gameMaster)
                .status(SessionStatus.CREATED)
                .initialBalance(request.getInitialBalance())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxPlayers(request.getMaxPlayers())
                .allowLateJoin(request.getAllowLateJoin() != null ? request.getAllowLateJoin() : false)
                .build();

        GameSession savedSession = gameSessionRepository.save(session);
        logger.info("Game session created successfully with ID: {}", savedSession.getId());

        return convertToDTO(savedSession);
    }

    @Override
    public GameSessionDTO updateSession(Long sessionId, Long gameMasterId, UpdateSessionRequest request) {
        logger.info("Updating session ID: {} by Game Master ID: {}", sessionId, gameMasterId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        // Can only update if not started
        if (session.getStatus() != SessionStatus.CREATED && session.getStatus() != SessionStatus.READY) {
            throw new IllegalArgumentException("Impossible de modifier une session déjà démarrée");
        }

        // Update fields if provided
        if (request.getName() != null) {
            session.setName(request.getName());
        }
        if (request.getDescription() != null) {
            session.setDescription(request.getDescription());
        }
        if (request.getInitialBalance() != null) {
            session.setInitialBalance(request.getInitialBalance());
        }
        if (request.getStartDate() != null) {
            session.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            session.setEndDate(request.getEndDate());
        }
        if (request.getMaxPlayers() != null) {
            session.setMaxPlayers(request.getMaxPlayers());
        }
        if (request.getAllowLateJoin() != null) {
            session.setAllowLateJoin(request.getAllowLateJoin());
        }

        GameSession updatedSession = gameSessionRepository.save(session);
        return convertToDTO(updatedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public GameSessionDTO getSessionById(Long sessionId) {
        GameSession session = getSessionEntity(sessionId);
        return convertToDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSessionDTO> getSessionsByGameMaster(Long gameMasterId) {
        List<GameSession> sessions = gameSessionRepository.findByGameMasterId(gameMasterId);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSessionDTO> getSessionsByStatus(SessionStatus status) {
        List<GameSession> sessions = gameSessionRepository.findByStatus(status);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSessionDTO> getActiveSessions() {
        List<GameSession> sessions = gameSessionRepository.findCurrentlyActiveSessions(LocalDateTime.now());
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSessionDTO> getUpcomingSessions() {
        List<GameSession> sessions = gameSessionRepository.findUpcomingSessions(LocalDateTime.now());
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionPlayerDTO addPlayerToSession(Long sessionId, Long gameMasterId, Long playerId) {
        logger.info("Adding player ID: {} to session ID: {}", playerId, sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        // Check if session can accept players
        if (!session.canAddPlayers()) {
            throw new IllegalArgumentException("Impossible d'ajouter des joueurs à cette session");
        }

        // Check if session is full
        if (session.isFull()) {
            throw new IllegalArgumentException("La session est complète");
        }

        // Check if player already in session
        if (gameSessionRepository.isPlayerInSession(sessionId, playerId)) {
            throw new IllegalArgumentException("Le joueur est déjà dans cette session");
        }

        // Get player
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur non trouvé avec l'ID: " + playerId));

        // Create session player
        SessionPlayer sessionPlayer = SessionPlayer.builder()
                .gameSession(session)
                .player(player)
                .initialBalance(session.getInitialBalance())
                .currentBalance(session.getInitialBalance())
                .portfolioValue(BigDecimal.ZERO)
                .totalValue(session.getInitialBalance())
                .tradesCount(0)
                .isActive(true)
                .build();

        SessionPlayer savedSessionPlayer = sessionPlayerRepository.save(sessionPlayer);
        logger.info("Player added successfully to session");

        return convertSessionPlayerToDTO(savedSessionPlayer);
    }

    @Override
    public void removePlayerFromSession(Long sessionId, Long gameMasterId, Long playerId) {
        logger.info("Removing player ID: {} from session ID: {}", playerId, sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        // Can only remove before session starts
        if (!session.canAddPlayers()) {
            throw new IllegalArgumentException("Impossible de retirer des joueurs d'une session démarrée");
        }

        SessionPlayer sessionPlayer = sessionPlayerRepository.findByGameSessionIdAndPlayerId(sessionId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur non trouvé dans cette session"));

        sessionPlayerRepository.delete(sessionPlayer);
        logger.info("Player removed successfully from session");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionPlayerDTO> getSessionPlayers(Long sessionId) {
        List<SessionPlayer> players = sessionPlayerRepository.findByGameSessionId(sessionId);
        return players.stream()
                .map(this::convertSessionPlayerToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SessionLeaderboardDTO getSessionLeaderboard(Long sessionId) {
        GameSession session = getSessionEntity(sessionId);
        List<SessionPlayer> players = sessionPlayerRepository.findLeaderboard(sessionId);
        
        return SessionLeaderboardDTO.builder()
                .sessionId(session.getId())
                .sessionName(session.getName())
                .players(players.stream()
                        .map(this::convertSessionPlayerToDTO)
                        .collect(Collectors.toList()))
                .totalPlayers(players.size())
                .build();
    }

    @Override
    public GameSessionDTO startSession(Long sessionId, Long gameMasterId) {
        logger.info("Starting session ID: {}", sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        if (!session.canStart()) {
            throw new IllegalArgumentException("La session ne peut pas être démarrée. Vérifiez le statut et le nombre de joueurs.");
        }

        session.setStatus(SessionStatus.ACTIVE);
        session.setActualStartTime(LocalDateTime.now());

        GameSession updatedSession = gameSessionRepository.save(session);
        logger.info("Session started successfully");

        return convertToDTO(updatedSession);
    }

    @Override
    public GameSessionDTO pauseSession(Long sessionId, Long gameMasterId) {
        logger.info("Pausing session ID: {}", sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Seule une session active peut être mise en pause");
        }

        session.setStatus(SessionStatus.PAUSED);
        GameSession updatedSession = gameSessionRepository.save(session);

        return convertToDTO(updatedSession);
    }

    @Override
    public GameSessionDTO resumeSession(Long sessionId, Long gameMasterId) {
        logger.info("Resuming session ID: {}", sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        if (session.getStatus() != SessionStatus.PAUSED) {
            throw new IllegalArgumentException("Seule une session en pause peut être reprise");
        }

        session.setStatus(SessionStatus.ACTIVE);
        GameSession updatedSession = gameSessionRepository.save(session);

        return convertToDTO(updatedSession);
    }

    @Override
    public GameSessionDTO completeSession(Long sessionId, Long gameMasterId) {
        logger.info("Completing session ID: {}", sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        if (session.isCompleted()) {
            throw new IllegalArgumentException("La session est déjà terminée");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setActualEndTime(LocalDateTime.now());

        // Update final rankings
        updateSessionRankings(sessionId);

        GameSession updatedSession = gameSessionRepository.save(session);
        logger.info("Session completed successfully");

        return convertToDTO(updatedSession);
    }

    @Override
    public GameSessionDTO cancelSession(Long sessionId, Long gameMasterId) {
        logger.info("Cancelling session ID: {}", sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        if (session.isCompleted()) {
            throw new IllegalArgumentException("Impossible d'annuler une session terminée");
        }

        session.setStatus(SessionStatus.CANCELLED);
        session.setActualEndTime(LocalDateTime.now());

        GameSession updatedSession = gameSessionRepository.save(session);
        logger.info("Session cancelled successfully");

        return convertToDTO(updatedSession);
    }

    @Override
    public void deleteSession(Long sessionId, Long gameMasterId) {
        logger.info("Deleting session ID: {}", sessionId);

        GameSession session = getSessionEntity(sessionId);
        validateGameMaster(session, gameMasterId);

        // Can only delete if not started
        if (session.getStatus() != SessionStatus.CREATED && session.getStatus() != SessionStatus.READY) {
            throw new IllegalArgumentException("Impossible de supprimer une session déjà démarrée");
        }

        gameSessionRepository.delete(session);
        logger.info("Session deleted successfully");
    }

    @Override
    public void updatePlayerStatistics(Long sessionId, Long playerId) {
        SessionPlayer sessionPlayer = sessionPlayerRepository.findByGameSessionIdAndPlayerId(sessionId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur non trouvé dans cette session"));

        // This would be called after a trade to update stats
        // The actual balance and portfolio value would be calculated elsewhere
        sessionPlayer.updateTotalValue();
        sessionPlayer.incrementTradesCount();
        
        sessionPlayerRepository.save(sessionPlayer);
    }

    @Override
    public void updateSessionRankings(Long sessionId) {
        logger.info("Updating rankings for session ID: {}", sessionId);

        List<SessionPlayer> players = sessionPlayerRepository.findLeaderboard(sessionId);
        
        int rank = 1;
        for (SessionPlayer player : players) {
            player.setRanking(rank++);
            sessionPlayerRepository.save(player);
        }

        logger.info("Rankings updated for {} players", players.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SessionPlayerDTO getPlayerPerformance(Long sessionId, Long playerId) {
        SessionPlayer sessionPlayer = sessionPlayerRepository.findByGameSessionIdAndPlayerId(sessionId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur non trouvé dans cette session"));
        
        return convertSessionPlayerToDTO(sessionPlayer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionPlayerDTO> getPlayerSessionHistory(Long playerId) {
        List<SessionPlayer> sessions = sessionPlayerRepository.findPlayerHistory(playerId);
        return sessions.stream()
                .map(this::convertSessionPlayerToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods

    private GameSession getSessionEntity(Long sessionId) {
        return gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée avec l'ID: " + sessionId));
    }

    private void validateGameMaster(GameSession session, Long gameMasterId) {
        if (!session.getGameMaster().getId().equals(gameMasterId)) {
            throw new IllegalArgumentException("Seul le Game Master de cette session peut effectuer cette opération");
        }
    }

    private GameSessionDTO convertToDTO(GameSession session) {
        return GameSessionDTO.builder()
                .id(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .gameMasterId(session.getGameMaster().getId())
                .gameMasterUsername(session.getGameMaster().getUsername())
                .gameMasterEmail(session.getGameMaster().getEmail())
                .status(session.getStatus())
                .initialBalance(session.getInitialBalance())
                .currency(session.getCurrency())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .actualStartTime(session.getActualStartTime())
                .actualEndTime(session.getActualEndTime())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .playerCount(session.getPlayerCount())
                .maxPlayers(session.getMaxPlayers())
                .allowLateJoin(session.getAllowLateJoin())
                .isFull(session.isFull())
                .build();
    }

    private SessionPlayerDTO convertSessionPlayerToDTO(SessionPlayer sp) {
        return SessionPlayerDTO.builder()
                .id(sp.getId())
                .sessionId(sp.getGameSession().getId())
                .sessionName(sp.getGameSession().getName())
                .playerId(sp.getPlayer().getId())
                .playerUsername(sp.getPlayer().getUsername())
                .playerEmail(sp.getPlayer().getEmail())
                .initialBalance(sp.getInitialBalance())
                .currentBalance(sp.getCurrentBalance())
                .portfolioValue(sp.getPortfolioValue())
                .totalValue(sp.getTotalValue())
                .profitLoss(sp.getProfitLoss())
                .profitLossPercentage(sp.getProfitLossPercentage())
                .ranking(sp.getRanking())
                .tradesCount(sp.getTradesCount())
                .joinedAt(sp.getJoinedAt())
                .lastActivityAt(sp.getLastActivityAt())
                .isActive(sp.getIsActive())
                .build();
    }
}

