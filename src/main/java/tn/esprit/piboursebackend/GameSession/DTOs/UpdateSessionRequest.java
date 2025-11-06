package tn.esprit.piboursebackend.GameSession.DTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionRequest {
    
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String name;
    
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;
    
    @DecimalMin(value = "0.01", message = "Le solde initial doit être supérieur à 0")
    private BigDecimal initialBalance;
    
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDate;
    
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDateTime endDate;
    
    private Integer maxPlayers;
    
    private Boolean allowLateJoin;
}

