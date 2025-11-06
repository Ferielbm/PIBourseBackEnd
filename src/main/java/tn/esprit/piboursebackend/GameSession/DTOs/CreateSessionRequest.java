package tn.esprit.piboursebackend.GameSession.DTOs;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    
    @NotBlank(message = "Le nom de la session est requis")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String name;
    
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;
    
    @NotNull(message = "Le solde initial est requis")
    @DecimalMin(value = "0.01", message = "Le solde initial doit être supérieur à 0")
    private BigDecimal initialBalance;
    
    @Size(max = 10, message = "Le code de devise ne peut pas dépasser 10 caractères")
    private String currency;
    
    @NotNull(message = "La date de début est requise")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDate;
    
    @NotNull(message = "La date de fin est requise")
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDateTime endDate;
    
    @Min(value = 2, message = "Le nombre minimum de joueurs est 2")
    private Integer maxPlayers;
    
    private Boolean allowLateJoin;
}

