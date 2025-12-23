package tn.esprit.piboursebackend.Player.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long playerId;

    private LocalDateTime lastLogin;

    private LocalDateTime lastActivity;

    @Builder.Default
    private Boolean isActive = false;

    // Méthode helper pour vérifier si le joueur est actif (< 15 minutes)
    public boolean isCurrentlyActive() {
        if (lastActivity == null) {
            return false;
        }
        return LocalDateTime.now().minusMinutes(15).isBefore(lastActivity);
    }

    // Méthode pour mettre à jour l'activité
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
        this.isActive = true;
    }

    // Méthode pour marquer la connexion
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.isActive = true;
    }
}
