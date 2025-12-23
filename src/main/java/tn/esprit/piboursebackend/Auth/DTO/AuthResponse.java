package tn.esprit.piboursebackend.Auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.piboursebackend.Player.Entities.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long playerId;
    private String username;
    private String email;
    private Role role;
    private String token; // Pour JWT (optionnel pour l'instant)
    private String message;
}
