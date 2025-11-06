package tn.esprit.piboursebackend.GameSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.piboursebackend.GameSession.DTOs.CreateSessionRequest;
import tn.esprit.piboursebackend.GameSession.DTOs.GameSessionDTO;
import tn.esprit.piboursebackend.GameSession.DTOs.SessionPlayerDTO;
import tn.esprit.piboursebackend.GameSession.Entities.GameSession;
import tn.esprit.piboursebackend.GameSession.Entities.SessionPlayer;
import tn.esprit.piboursebackend.GameSession.Entities.SessionStatus;
import tn.esprit.piboursebackend.GameSession.Repositories.GameSessionRepository;
import tn.esprit.piboursebackend.GameSession.Repositories.SessionPlayerRepository;
import tn.esprit.piboursebackend.GameSession.Services.GameSessionService;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Exceptions.ResourceNotFoundException;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service GameSession")
class GameSessionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private SessionPlayerRepository sessionPlayerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GameSessionService gameSessionService;

    private Player gameMaster;
    private Player player;
    private GameSession gameSession;
    private CreateSessionRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup Game Master
        gameMaster = new Player();
        gameMaster.setId(1L);
        gameMaster.setUsername("GameMaster1");
        gameMaster.setEmail("gm@example.com");
        gameMaster.setPassword("password123");
        gameMaster.setRole(Role.ROLE_GAME_MASTER);

        // Setup Player
        player = new Player();
        player.setId(2L);
        player.setUsername("Player1");
        player.setEmail("player@example.com");
        player.setPassword("password123");
        player.setRole(Role.ROLE_PLAYER);

        // Setup CreateSessionRequest
        createRequest = new CreateSessionRequest();
        createRequest.setName("Test Session");
        createRequest.setDescription("Test description");
        createRequest.setInitialBalance(new BigDecimal("10000.00"));
        createRequest.setCurrency("USD");
        createRequest.setStartDate(LocalDateTime.now().plusDays(1));
        createRequest.setEndDate(LocalDateTime.now().plusDays(7));
        createRequest.setMaxPlayers(10);
        createRequest.setAllowLateJoin(false);

        // Setup GameSession
        gameSession = GameSession.builder()
                .id(1L)
                .name("Test Session")
                .description("Test description")
                .gameMaster(gameMaster)
                .status(SessionStatus.CREATED)
                .initialBalance(new BigDecimal("10000.00"))
                .currency("USD")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .maxPlayers(10)
                .allowLateJoin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Créer une session - Succès")
    void testCreateSession_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(gameMaster));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(gameSession);

        // When
        GameSessionDTO result = gameSessionService.createSession(1L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Session", result.getName());
        assertEquals("Test description", result.getDescription());
        assertEquals(SessionStatus.CREATED, result.getStatus());
        assertEquals(new BigDecimal("10000.00"), result.getInitialBalance());
        assertEquals("USD", result.getCurrency());

        verify(playerRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Créer une session - Game Master non trouvé")
    void testCreateSession_GameMasterNotFound() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            gameSessionService.createSession(999L, createRequest);
        });

        verify(playerRepository, times(1)).findById(999L);
        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Créer une session - Date de fin avant date de début")
    void testCreateSession_InvalidDates() {
        // Given
        createRequest.setStartDate(LocalDateTime.now().plusDays(7));
        createRequest.setEndDate(LocalDateTime.now().plusDays(1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.createSession(1L, createRequest);
        });

        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Récupérer une session par ID - Succès")
    void testGetSessionById_Success() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));

        // When
        GameSessionDTO result = gameSessionService.getSessionById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Session", result.getName());

        verify(gameSessionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Récupérer une session par ID - Non trouvée")
    void testGetSessionById_NotFound() {
        // Given
        when(gameSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            gameSessionService.getSessionById(999L);
        });

        verify(gameSessionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Récupérer les sessions d'un Game Master")
    void testGetSessionsByGameMaster() {
        // Given
        List<GameSession> sessions = Arrays.asList(gameSession);
        when(gameSessionRepository.findByGameMasterId(1L)).thenReturn(sessions);

        // When
        List<GameSessionDTO> result = gameSessionService.getSessionsByGameMaster(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Session", result.get(0).getName());

        verify(gameSessionRepository, times(1)).findByGameMasterId(1L);
    }

    @Test
    @DisplayName("Ajouter un joueur à une session - Succès")
    void testAddPlayerToSession_Success() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player));
        when(gameSessionRepository.isPlayerInSession(1L, 2L)).thenReturn(false);

        SessionPlayer sessionPlayer = SessionPlayer.builder()
                .id(1L)
                .gameSession(gameSession)
                .player(player)
                .initialBalance(new BigDecimal("10000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .portfolioValue(BigDecimal.ZERO)
                .totalValue(new BigDecimal("10000.00"))
                .tradesCount(0)
                .isActive(true)
                .joinedAt(LocalDateTime.now())
                .build();

        when(sessionPlayerRepository.save(any(SessionPlayer.class))).thenReturn(sessionPlayer);

        // When
        SessionPlayerDTO result = gameSessionService.addPlayerToSession(1L, 1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getPlayerId());
        assertEquals("Player1", result.getPlayerUsername());
        assertEquals(new BigDecimal("10000.00"), result.getInitialBalance());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(playerRepository, times(1)).findById(2L);
        verify(sessionPlayerRepository, times(1)).save(any(SessionPlayer.class));
    }

    @Test
    @DisplayName("Ajouter un joueur - Joueur déjà dans la session")
    void testAddPlayerToSession_PlayerAlreadyInSession() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(gameSessionRepository.isPlayerInSession(1L, 2L)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.addPlayerToSession(1L, 1L, 2L);
        });

        verify(sessionPlayerRepository, never()).save(any(SessionPlayer.class));
    }

    @Test
    @DisplayName("Ajouter un joueur - Utilisateur non Game Master de cette session")
    void testAddPlayerToSession_NotGameMaster() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.addPlayerToSession(1L, 999L, 2L);
        });

        verify(sessionPlayerRepository, never()).save(any(SessionPlayer.class));
    }

    @Test
    @DisplayName("Démarrer une session - Succès")
    void testStartSession_Success() {
        // Given
        SessionPlayer sessionPlayer = SessionPlayer.builder()
                .id(1L)
                .gameSession(gameSession)
                .player(player)
                .initialBalance(new BigDecimal("10000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .portfolioValue(BigDecimal.ZERO)
                .totalValue(new BigDecimal("10000.00"))
                .build();

        gameSession.addPlayer(sessionPlayer);

        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(gameSession);

        // When
        GameSessionDTO result = gameSessionService.startSession(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(SessionStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getActualStartTime());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Démarrer une session - Aucun joueur ajouté")
    void testStartSession_NoPlayers() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.startSession(1L, 1L);
        });

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Terminer une session - Succès")
    void testCompleteSession_Success() {
        // Given
        gameSession.setStatus(SessionStatus.ACTIVE);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(gameSession);
        when(sessionPlayerRepository.findLeaderboard(1L)).thenReturn(Arrays.asList());

        // When
        GameSessionDTO result = gameSessionService.completeSession(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(SessionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getActualEndTime());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
        verify(sessionPlayerRepository, times(1)).findLeaderboard(1L);
    }

    @Test
    @DisplayName("Terminer une session - Session déjà terminée")
    void testCompleteSession_AlreadyCompleted() {
        // Given
        gameSession.setStatus(SessionStatus.COMPLETED);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.completeSession(1L, 1L);
        });

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(sessionPlayerRepository, never()).findLeaderboard(anyLong());
    }

    @Test
    @DisplayName("Mettre en pause une session - Succès")
    void testPauseSession_Success() {
        // Given
        gameSession.setStatus(SessionStatus.ACTIVE);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(gameSession);

        // When
        GameSessionDTO result = gameSessionService.pauseSession(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(SessionStatus.PAUSED, result.getStatus());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Reprendre une session - Succès")
    void testResumeSession_Success() {
        // Given
        gameSession.setStatus(SessionStatus.PAUSED);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(gameSession);

        // When
        GameSessionDTO result = gameSessionService.resumeSession(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(SessionStatus.ACTIVE, result.getStatus());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Annuler une session - Succès")
    void testCancelSession_Success() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(gameSession);

        // When
        GameSessionDTO result = gameSessionService.cancelSession(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(SessionStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getActualEndTime());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Supprimer une session - Succès")
    void testDeleteSession_Success() {
        // Given
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        doNothing().when(gameSessionRepository).delete(any(GameSession.class));

        // When
        gameSessionService.deleteSession(1L, 1L);

        // Then
        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, times(1)).delete(gameSession);
    }

    @Test
    @DisplayName("Supprimer une session - Session déjà démarrée")
    void testDeleteSession_AlreadyStarted() {
        // Given
        gameSession.setStatus(SessionStatus.ACTIVE);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.deleteSession(1L, 1L);
        });

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(gameSessionRepository, never()).delete(any(GameSession.class));
    }

    @Test
    @DisplayName("Retirer un joueur d'une session - Succès")
    void testRemovePlayerFromSession_Success() {
        // Given
        SessionPlayer sessionPlayer = SessionPlayer.builder()
                .id(1L)
                .gameSession(gameSession)
                .player(player)
                .build();

        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(sessionPlayerRepository.findByGameSessionIdAndPlayerId(1L, 2L))
                .thenReturn(Optional.of(sessionPlayer));
        doNothing().when(sessionPlayerRepository).delete(any(SessionPlayer.class));

        // When
        gameSessionService.removePlayerFromSession(1L, 1L, 2L);

        // Then
        verify(gameSessionRepository, times(1)).findById(1L);
        verify(sessionPlayerRepository, times(1)).findByGameSessionIdAndPlayerId(1L, 2L);
        verify(sessionPlayerRepository, times(1)).delete(sessionPlayer);
    }

    @Test
    @DisplayName("Retirer un joueur - Session déjà démarrée")
    void testRemovePlayerFromSession_SessionStarted() {
        // Given
        gameSession.setStatus(SessionStatus.ACTIVE);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gameSessionService.removePlayerFromSession(1L, 1L, 2L);
        });

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(sessionPlayerRepository, never()).delete(any(SessionPlayer.class));
    }

    @Test
    @DisplayName("Récupérer les joueurs d'une session")
    void testGetSessionPlayers() {
        // Given
        SessionPlayer sessionPlayer = SessionPlayer.builder()
                .id(1L)
                .gameSession(gameSession)
                .player(player)
                .initialBalance(new BigDecimal("10000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .portfolioValue(BigDecimal.ZERO)
                .totalValue(new BigDecimal("10000.00"))
                .joinedAt(LocalDateTime.now())
                .build();

        when(sessionPlayerRepository.findByGameSessionId(1L)).thenReturn(Arrays.asList(sessionPlayer));

        // When
        List<SessionPlayerDTO> result = gameSessionService.getSessionPlayers(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Player1", result.get(0).getPlayerUsername());

        verify(sessionPlayerRepository, times(1)).findByGameSessionId(1L);
    }

    @Test
    @DisplayName("Récupérer le classement d'une session")
    void testGetSessionLeaderboard() {
        // Given
        SessionPlayer sessionPlayer = SessionPlayer.builder()
                .id(1L)
                .gameSession(gameSession)
                .player(player)
                .initialBalance(new BigDecimal("10000.00"))
                .currentBalance(new BigDecimal("12000.00"))
                .portfolioValue(new BigDecimal("3000.00"))
                .totalValue(new BigDecimal("15000.00"))
                .ranking(1)
                .joinedAt(LocalDateTime.now())
                .build();

        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(gameSession));
        when(sessionPlayerRepository.findLeaderboard(1L)).thenReturn(Arrays.asList(sessionPlayer));

        // When
        var leaderboard = gameSessionService.getSessionLeaderboard(1L);

        // Then
        assertNotNull(leaderboard);
        assertEquals(1L, leaderboard.getSessionId());
        assertEquals("Test Session", leaderboard.getSessionName());
        assertEquals(1, leaderboard.getTotalPlayers());

        verify(gameSessionRepository, times(1)).findById(1L);
        verify(sessionPlayerRepository, times(1)).findLeaderboard(1L);
    }
}


