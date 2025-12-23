package tn.esprit.piboursebackend.Player.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.PlayerActivity;
import tn.esprit.piboursebackend.Player.Repositories.PlayerActivityRepository;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerActivityService {

    private final PlayerActivityRepository activityRepository;
    private final PlayerRepository playerRepository;

    private static final int ACTIVE_THRESHOLD_MINUTES = 15;

    /**
     * Enregistrer une connexion
     */
    @Transactional
    public void recordLogin(Long playerId) {
        PlayerActivity activity = activityRepository.findByPlayerId(playerId)
                .orElse(PlayerActivity.builder()
                        .playerId(playerId)
                        .build());

        activity.recordLogin();
        activityRepository.save(activity);
        
        log.info("🟢 Login enregistré pour le joueur {}", playerId);
    }

    /**
     * Mettre à jour l'activité d'un joueur
     */
    @Transactional
    public void updateActivity(Long playerId) {
        PlayerActivity activity = activityRepository.findByPlayerId(playerId)
                .orElse(PlayerActivity.builder()
                        .playerId(playerId)
                        .build());

        activity.updateActivity();
        activityRepository.save(activity);
    }

    /**
     * Récupérer tous les joueurs actifs (< 15 minutes)
     */
    public List<PlayerActivityDTO> getActivePlayers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(ACTIVE_THRESHOLD_MINUTES);
        log.info("🔍 DEBUG - Seuil d'activité (threshold): {}", threshold);
        
        List<PlayerActivity> activities = activityRepository.findActivePlayers(threshold);
        log.info("🔍 DEBUG - Joueurs actifs trouvés dans la BDD: {}", activities.size());
        
        if (activities.isEmpty()) {
            log.warn("⚠️  ATTENTION - Aucun joueur actif trouvé! Vérifier la table PlayerActivity");
            // Récupérer TOUS les enregistrements pour debug
            List<PlayerActivity> all = activityRepository.findAll();
            log.warn("📊 Total d'enregistrements PlayerActivity: {}", all.size());
            if (!all.isEmpty()) {
                all.stream().limit(5).forEach(pa -> 
                    log.warn("   - PlayerId: {}, LastActivity: {}", pa.getPlayerId(), pa.getLastActivity())
                );
            }
        }

        return activities.stream()
                .map(activity -> {
                    Player player = playerRepository.findById(activity.getPlayerId()).orElse(null);
                    if (player != null) {
                        return PlayerActivityDTO.builder()
                                .playerId(player.getId())
                                .username(player.getUsername())
                                .email(player.getEmail())
                                .role(player.getRole())
                                .lastLogin(activity.getLastLogin())
                                .lastActivity(activity.getLastActivity())
                                .isActive(activity.isCurrentlyActive())
                                .build();
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Compter les joueurs actifs
     */
    public Long countActivePlayers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(ACTIVE_THRESHOLD_MINUTES);
        return activityRepository.countActivePlayers(threshold);
    }

    /**
     * Récupérer l'activité d'un joueur spécifique
     */
    public PlayerActivityDTO getPlayerActivity(Long playerId) {
        PlayerActivity activity = activityRepository.findByPlayerId(playerId)
                .orElse(null);

        if (activity == null) {
            return null;
        }

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            return null;
        }

        return PlayerActivityDTO.builder()
                .playerId(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .role(player.getRole())
                .lastLogin(activity.getLastLogin())
                .lastActivity(activity.getLastActivity())
                .isActive(activity.isCurrentlyActive())
                .build();
    }

    /**
     * Récupérer les joueurs connectés aujourd'hui
     */
    public List<PlayerActivityDTO> getPlayersLoggedInToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<PlayerActivity> activities = activityRepository.findPlayersLoggedInToday(startOfDay);

        return activities.stream()
                .map(activity -> {
                    Player player = playerRepository.findById(activity.getPlayerId()).orElse(null);
                    if (player != null) {
                        return PlayerActivityDTO.builder()
                                .playerId(player.getId())
                                .username(player.getUsername())
                                .email(player.getEmail())
                                .role(player.getRole())
                                .lastLogin(activity.getLastLogin())
                                .lastActivity(activity.getLastActivity())
                                .isActive(activity.isCurrentlyActive())
                                .build();
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    // DTO interne
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlayerActivityDTO {
        private Long playerId;
        private String username;
        private String email;
        private tn.esprit.piboursebackend.Player.Entities.Role role;
        private LocalDateTime lastLogin;
        private LocalDateTime lastActivity;
        private Boolean isActive;
    }
}
