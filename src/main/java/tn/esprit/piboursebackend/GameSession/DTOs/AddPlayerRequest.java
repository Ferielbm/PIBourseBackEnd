package tn.esprit.piboursebackend.GameSession.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPlayerRequest {
    
    @NotNull(message = "L'ID du joueur est requis")
    private Long playerId;
    
    private String notes;  // Notes optionnelles du Game Master
}

