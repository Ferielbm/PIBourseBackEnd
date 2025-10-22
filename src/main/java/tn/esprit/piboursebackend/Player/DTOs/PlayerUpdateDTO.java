package tn.esprit.piboursebackend.Player.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.piboursebackend.Player.Entities.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUpdateDTO {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;
    
    private Role role;
}

