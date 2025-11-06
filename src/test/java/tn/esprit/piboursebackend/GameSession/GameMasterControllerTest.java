package tn.esprit.piboursebackend.GameSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.piboursebackend.GameSession.DTOs.*;
import tn.esprit.piboursebackend.GameSession.Entities.SessionStatus;
import tn.esprit.piboursebackend.GameSession.Services.IGameSessionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests d'intégration du contrôleur GameMaster")
class GameMasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IGameSessionService gameSessionService;

    private GameSessionDTO sessionDTO;
    private CreateSessionRequest createRequest;
    private SessionPlayerDTO playerDTO;

    @BeforeEach
    void setUp() {
        // Setup DTO de session
        sessionDTO = GameSessionDTO.builder()
                .id(1L)
                .name("Test Session")
                .description("Test description")
                .gameMasterId(1L)
                .gameMasterUsername("GameMaster1")
                .gameMasterEmail("gm@example.com")
                .status(SessionStatus.CREATED)
                .initialBalance(new BigDecimal("10000.00"))
                .currency("USD")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .playerCount(0)
                .maxPlayers(10)
                .allowLateJoin(false)
                .isFull(false)
                .build();

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

        // Setup SessionPlayerDTO
        playerDTO = SessionPlayerDTO.builder()
                .id(1L)
                .sessionId(1L)
                .sessionName("Test Session")
                .playerId(2L)
                .playerUsername("Player1")
                .playerEmail("player@example.com")
                .initialBalance(new BigDecimal("10000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .portfolioValue(BigDecimal.ZERO)
                .totalValue(new BigDecimal("10000.00"))
                .tradesCount(0)
                .isActive(true)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/game-master/sessions - Créer une session (avec authentification)")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testCreateSession_Success() throws Exception {
        // Given
        when(gameSessionService.createSession(anyLong(), any(CreateSessionRequest.class)))
                .thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Session"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.initialBalance").value(10000.00));

        verify(gameSessionService, times(1)).createSession(anyLong(), any(CreateSessionRequest.class));
    }

    @Test
    @DisplayName("POST /api/game-master/sessions - Sans authentification (401)")
    void testCreateSession_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/game-master/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(gameSessionService, never()).createSession(anyLong(), any(CreateSessionRequest.class));
    }

    @Test
    @DisplayName("POST /api/game-master/sessions - Avec rôle PLAYER (403)")
    @WithMockUser(username = "Player1", roles = {"PLAYER"})
    void testCreateSession_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/game-master/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(gameSessionService, never()).createSession(anyLong(), any(CreateSessionRequest.class));
    }

    @Test
    @DisplayName("GET /api/game-master/sessions/{id} - Récupérer une session")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testGetSession_Success() throws Exception {
        // Given
        when(gameSessionService.getSessionById(1L)).thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(get("/api/game-master/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Session"));

        verify(gameSessionService, times(1)).getSessionById(1L);
    }

    @Test
    @DisplayName("GET /api/game-master/my-sessions - Récupérer mes sessions")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testGetMySessions_Success() throws Exception {
        // Given
        List<GameSessionDTO> sessions = Arrays.asList(sessionDTO);
        when(gameSessionService.getSessionsByGameMaster(anyLong())).thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/game-master/my-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Session"));

        verify(gameSessionService, times(1)).getSessionsByGameMaster(anyLong());
    }

    @Test
    @DisplayName("GET /api/game-master/sessions/active - Sessions actives")
    @WithMockUser(username = "Player1", roles = {"PLAYER"})
    void testGetActiveSessions_Success() throws Exception {
        // Given
        sessionDTO.setStatus(SessionStatus.ACTIVE);
        List<GameSessionDTO> sessions = Arrays.asList(sessionDTO);
        when(gameSessionService.getActiveSessions()).thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/game-master/sessions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(gameSessionService, times(1)).getActiveSessions();
    }

    @Test
    @DisplayName("PUT /api/game-master/sessions/{id} - Modifier une session")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testUpdateSession_Success() throws Exception {
        // Given
        UpdateSessionRequest updateRequest = new UpdateSessionRequest();
        updateRequest.setName("Updated Session");

        sessionDTO.setName("Updated Session");
        when(gameSessionService.updateSession(eq(1L), anyLong(), any(UpdateSessionRequest.class)))
                .thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(put("/api/game-master/sessions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Session"));

        verify(gameSessionService, times(1)).updateSession(eq(1L), anyLong(), any(UpdateSessionRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/game-master/sessions/{id} - Supprimer une session")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testDeleteSession_Success() throws Exception {
        // Given
        doNothing().when(gameSessionService).deleteSession(eq(1L), anyLong());

        // When & Then
        mockMvc.perform(delete("/api/game-master/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session supprimée avec succès"));

        verify(gameSessionService, times(1)).deleteSession(eq(1L), anyLong());
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/players - Ajouter un joueur")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testAddPlayer_Success() throws Exception {
        // Given
        AddPlayerRequest addRequest = new AddPlayerRequest();
        addRequest.setPlayerId(2L);

        when(gameSessionService.addPlayerToSession(eq(1L), anyLong(), eq(2L)))
                .thenReturn(playerDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value(2))
                .andExpect(jsonPath("$.playerUsername").value("Player1"));

        verify(gameSessionService, times(1)).addPlayerToSession(eq(1L), anyLong(), eq(2L));
    }

    @Test
    @DisplayName("DELETE /api/game-master/sessions/{id}/players/{playerId} - Retirer un joueur")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testRemovePlayer_Success() throws Exception {
        // Given
        doNothing().when(gameSessionService).removePlayerFromSession(eq(1L), anyLong(), eq(2L));

        // When & Then
        mockMvc.perform(delete("/api/game-master/sessions/1/players/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Joueur retiré avec succès"));

        verify(gameSessionService, times(1)).removePlayerFromSession(eq(1L), anyLong(), eq(2L));
    }

    @Test
    @DisplayName("GET /api/game-master/sessions/{id}/players - Liste des joueurs")
    @WithMockUser(username = "Player1", roles = {"PLAYER"})
    void testGetSessionPlayers_Success() throws Exception {
        // Given
        List<SessionPlayerDTO> players = Arrays.asList(playerDTO);
        when(gameSessionService.getSessionPlayers(1L)).thenReturn(players);

        // When & Then
        mockMvc.perform(get("/api/game-master/sessions/1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].playerUsername").value("Player1"));

        verify(gameSessionService, times(1)).getSessionPlayers(1L);
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/start - Démarrer une session")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testStartSession_Success() throws Exception {
        // Given
        sessionDTO.setStatus(SessionStatus.ACTIVE);
        sessionDTO.setActualStartTime(LocalDateTime.now());
        when(gameSessionService.startSession(eq(1L), anyLong())).thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.actualStartTime").exists());

        verify(gameSessionService, times(1)).startSession(eq(1L), anyLong());
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/pause - Mettre en pause")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testPauseSession_Success() throws Exception {
        // Given
        sessionDTO.setStatus(SessionStatus.PAUSED);
        when(gameSessionService.pauseSession(eq(1L), anyLong())).thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));

        verify(gameSessionService, times(1)).pauseSession(eq(1L), anyLong());
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/resume - Reprendre")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testResumeSession_Success() throws Exception {
        // Given
        sessionDTO.setStatus(SessionStatus.ACTIVE);
        when(gameSessionService.resumeSession(eq(1L), anyLong())).thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(gameSessionService, times(1)).resumeSession(eq(1L), anyLong());
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/complete - Terminer une session")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testCompleteSession_Success() throws Exception {
        // Given
        sessionDTO.setStatus(SessionStatus.COMPLETED);
        sessionDTO.setActualEndTime(LocalDateTime.now());
        when(gameSessionService.completeSession(eq(1L), anyLong())).thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.actualEndTime").exists());

        verify(gameSessionService, times(1)).completeSession(eq(1L), anyLong());
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/cancel - Annuler une session")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testCancelSession_Success() throws Exception {
        // Given
        sessionDTO.setStatus(SessionStatus.CANCELLED);
        when(gameSessionService.cancelSession(eq(1L), anyLong())).thenReturn(sessionDTO);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(gameSessionService, times(1)).cancelSession(eq(1L), anyLong());
    }

    @Test
    @DisplayName("GET /api/game-master/sessions/{id}/leaderboard - Récupérer le classement")
    @WithMockUser(username = "Player1", roles = {"PLAYER"})
    void testGetLeaderboard_Success() throws Exception {
        // Given
        SessionLeaderboardDTO leaderboard = SessionLeaderboardDTO.builder()
                .sessionId(1L)
                .sessionName("Test Session")
                .players(Arrays.asList(playerDTO))
                .totalPlayers(1)
                .build();

        when(gameSessionService.getSessionLeaderboard(1L)).thenReturn(leaderboard);

        // When & Then
        mockMvc.perform(get("/api/game-master/sessions/1/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.sessionName").value("Test Session"))
                .andExpect(jsonPath("$.totalPlayers").value(1))
                .andExpect(jsonPath("$.players.length()").value(1));

        verify(gameSessionService, times(1)).getSessionLeaderboard(1L);
    }

    @Test
    @DisplayName("POST /api/game-master/sessions/{id}/update-rankings - Recalculer les classements")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testUpdateRankings_Success() throws Exception {
        // Given
        when(gameSessionService.getSessionById(1L)).thenReturn(sessionDTO);
        doNothing().when(gameSessionService).updateSessionRankings(1L);

        // When & Then
        mockMvc.perform(post("/api/game-master/sessions/1/update-rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Classements mis à jour avec succès"));

        verify(gameSessionService, times(1)).updateSessionRankings(1L);
    }

    @Test
    @DisplayName("GET /api/game-master/sessions/{id}/players/{playerId}/performance - Performance d'un joueur")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testGetPlayerPerformance_Success() throws Exception {
        // Given
        when(gameSessionService.getPlayerPerformance(1L, 2L)).thenReturn(playerDTO);

        // When & Then
        mockMvc.perform(get("/api/game-master/sessions/1/players/2/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(2))
                .andExpect(jsonPath("$.playerUsername").value("Player1"));

        verify(gameSessionService, times(1)).getPlayerPerformance(1L, 2L);
    }

    @Test
    @DisplayName("GET /api/game-master/players/{playerId}/history - Historique d'un joueur")
    @WithMockUser(username = "GameMaster1", roles = {"GAME_MASTER"})
    void testGetPlayerHistory_Success() throws Exception {
        // Given
        List<SessionPlayerDTO> history = Arrays.asList(playerDTO);
        when(gameSessionService.getPlayerSessionHistory(2L)).thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/game-master/players/2/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].playerId").value(2));

        verify(gameSessionService, times(1)).getPlayerSessionHistory(2L);
    }
}


