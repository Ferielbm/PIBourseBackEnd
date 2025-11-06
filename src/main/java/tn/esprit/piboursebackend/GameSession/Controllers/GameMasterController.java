package tn.esprit.piboursebackend.GameSession.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.GameSession.DTOs.*;
import tn.esprit.piboursebackend.GameSession.Services.IGameSessionService;
import tn.esprit.piboursebackend.Player.Security.UserDetailsImpl;
import tn.esprit.piboursebackend.Player.Security.dto.MessageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/game-master")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Game Master", description = "API de gestion des sessions de jeu")
@SecurityRequirement(name = "bearerAuth")
public class GameMasterController {

    @Autowired
    private IGameSessionService gameSessionService;

    // ==================== Session Management ====================

    /**
     * Create a new game session
     */
    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Créer une session", description = "Crée une nouvelle session de jeu")
    public ResponseEntity<?> createSession(@Valid @RequestBody CreateSessionRequest request,
                                          Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.createSession(userDetails.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Update an existing session
     */
    @PutMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Modifier une session", description = "Modifie une session existante (si non démarrée)")
    public ResponseEntity<?> updateSession(@PathVariable Long sessionId,
                                          @Valid @RequestBody UpdateSessionRequest request,
                                          Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.updateSession(sessionId, userDetails.getId(), request);
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Get session by ID
     */
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir une session", description = "Récupère les détails d'une session")
    public ResponseEntity<GameSessionDTO> getSession(@PathVariable Long sessionId) {
        GameSessionDTO session = gameSessionService.getSessionById(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * Get all my sessions (as game master)
     */
    @GetMapping("/my-sessions")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Mes sessions", description = "Récupère toutes les sessions que j'ai créées")
    public ResponseEntity<List<GameSessionDTO>> getMySessions(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<GameSessionDTO> sessions = gameSessionService.getSessionsByGameMaster(userDetails.getId());
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get all active sessions
     */
    @GetMapping("/sessions/active")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Sessions actives", description = "Récupère toutes les sessions actives")
    public ResponseEntity<List<GameSessionDTO>> getActiveSessions() {
        List<GameSessionDTO> sessions = gameSessionService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get upcoming sessions
     */
    @GetMapping("/sessions/upcoming")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Sessions à venir", description = "Récupère toutes les sessions à venir")
    public ResponseEntity<List<GameSessionDTO>> getUpcomingSessions() {
        List<GameSessionDTO> sessions = gameSessionService.getUpcomingSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Delete a session
     */
    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Supprimer une session", description = "Supprime une session non démarrée")
    public ResponseEntity<?> deleteSession(@PathVariable Long sessionId,
                                          Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            gameSessionService.deleteSession(sessionId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Session supprimée avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ==================== Player Management ====================

    /**
     * Add a player to a session
     */
    @PostMapping("/sessions/{sessionId}/players")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Ajouter un joueur", description = "Ajoute un joueur à une session")
    public ResponseEntity<?> addPlayer(@PathVariable Long sessionId,
                                      @Valid @RequestBody AddPlayerRequest request,
                                      Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            SessionPlayerDTO player = gameSessionService.addPlayerToSession(
                    sessionId, userDetails.getId(), request.getPlayerId());
            return ResponseEntity.status(HttpStatus.CREATED).body(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Remove a player from a session
     */
    @DeleteMapping("/sessions/{sessionId}/players/{playerId}")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Retirer un joueur", description = "Retire un joueur d'une session")
    public ResponseEntity<?> removePlayer(@PathVariable Long sessionId,
                                         @PathVariable Long playerId,
                                         Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            gameSessionService.removePlayerFromSession(sessionId, userDetails.getId(), playerId);
            return ResponseEntity.ok(new MessageResponse("Joueur retiré avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Get all players in a session
     */
    @GetMapping("/sessions/{sessionId}/players")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Liste des joueurs", description = "Récupère tous les joueurs d'une session")
    public ResponseEntity<List<SessionPlayerDTO>> getSessionPlayers(@PathVariable Long sessionId) {
        List<SessionPlayerDTO> players = gameSessionService.getSessionPlayers(sessionId);
        return ResponseEntity.ok(players);
    }

    // ==================== Session Control ====================

    /**
     * Start a session
     */
    @PostMapping("/sessions/{sessionId}/start")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Démarrer une session", description = "Démarre une session de jeu")
    public ResponseEntity<?> startSession(@PathVariable Long sessionId,
                                         Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.startSession(sessionId, userDetails.getId());
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Pause a session
     */
    @PostMapping("/sessions/{sessionId}/pause")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Mettre en pause", description = "Met une session active en pause")
    public ResponseEntity<?> pauseSession(@PathVariable Long sessionId,
                                         Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.pauseSession(sessionId, userDetails.getId());
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Resume a paused session
     */
    @PostMapping("/sessions/{sessionId}/resume")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Reprendre une session", description = "Reprend une session en pause")
    public ResponseEntity<?> resumeSession(@PathVariable Long sessionId,
                                          Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.resumeSession(sessionId, userDetails.getId());
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Complete a session
     */
    @PostMapping("/sessions/{sessionId}/complete")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Terminer une session", description = "Clôture une session et calcule les résultats finaux")
    public ResponseEntity<?> completeSession(@PathVariable Long sessionId,
                                            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.completeSession(sessionId, userDetails.getId());
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Cancel a session
     */
    @PostMapping("/sessions/{sessionId}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Annuler une session", description = "Annule une session")
    public ResponseEntity<?> cancelSession(@PathVariable Long sessionId,
                                          Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            GameSessionDTO session = gameSessionService.cancelSession(sessionId, userDetails.getId());
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ==================== Leaderboard & Statistics ====================

    /**
     * Get session leaderboard
     */
    @GetMapping("/sessions/{sessionId}/leaderboard")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Classement", description = "Récupère le classement des joueurs dans une session")
    public ResponseEntity<SessionLeaderboardDTO> getLeaderboard(@PathVariable Long sessionId) {
        SessionLeaderboardDTO leaderboard = gameSessionService.getSessionLeaderboard(sessionId);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Update session rankings
     */
    @PostMapping("/sessions/{sessionId}/update-rankings")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Mettre à jour les classements", description = "Recalcule les classements de la session")
    public ResponseEntity<?> updateRankings(@PathVariable Long sessionId,
                                           Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            // Validate game master ownership first
            GameSessionDTO session = gameSessionService.getSessionById(sessionId);
            if (!session.getGameMasterId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Accès refusé"));
            }
            
            gameSessionService.updateSessionRankings(sessionId);
            return ResponseEntity.ok(new MessageResponse("Classements mis à jour avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Get player performance in a session
     */
    @GetMapping("/sessions/{sessionId}/players/{playerId}/performance")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Performance d'un joueur", description = "Récupère les statistiques d'un joueur dans une session")
    public ResponseEntity<SessionPlayerDTO> getPlayerPerformance(@PathVariable Long sessionId,
                                                                 @PathVariable Long playerId) {
        SessionPlayerDTO performance = gameSessionService.getPlayerPerformance(sessionId, playerId);
        return ResponseEntity.ok(performance);
    }

    /**
     * Get player's session history
     */
    @GetMapping("/players/{playerId}/history")
    @PreAuthorize("hasAnyRole('ROLE_GAME_MASTER', 'ROLE_ADMIN')")
    @Operation(summary = "Historique d'un joueur", description = "Récupère l'historique des sessions d'un joueur")
    public ResponseEntity<List<SessionPlayerDTO>> getPlayerHistory(@PathVariable Long playerId) {
        List<SessionPlayerDTO> history = gameSessionService.getPlayerSessionHistory(playerId);
        return ResponseEntity.ok(history);
    }
}

