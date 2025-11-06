package tn.esprit.piboursebackend.Player.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    
    private Long id;
    private Long playerId;
    private String playerUsername;
    private String playerEmail;
    private BigDecimal balance;
    private String currency;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

