package tn.esprit.piboursebackend.Player.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.piboursebackend.Player.Entities.TransactionType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private TransactionType type;
    private Double amount;
    private LocalDateTime createdAt;
    private Long playerId;
    private String playerUsername;
}

