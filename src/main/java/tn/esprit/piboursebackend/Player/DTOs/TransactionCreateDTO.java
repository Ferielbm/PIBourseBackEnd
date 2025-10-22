package tn.esprit.piboursebackend.Player.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.piboursebackend.Player.Entities.TransactionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateDTO {
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotNull(message = "Player ID is required")
    private Long playerId;
}

