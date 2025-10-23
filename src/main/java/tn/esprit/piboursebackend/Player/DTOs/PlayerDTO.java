package tn.esprit.piboursebackend.Player.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.piboursebackend.Player.Entities.Role;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PlayerDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;


}

