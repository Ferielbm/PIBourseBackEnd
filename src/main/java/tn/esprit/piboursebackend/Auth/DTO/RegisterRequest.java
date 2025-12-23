package tn.esprit.piboursebackend.Auth.DTO;

import lombok.Data;
import tn.esprit.piboursebackend.Player.Entities.Role;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Role role; // ROLE_PLAYER par défaut, ROLE_MENEUR_JEU si spécifié
}
