package tn.esprit.piboursebackend.GameSession.DTOs;

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
public class SessionPlayerDTO {
    
    private Long id;
    private Long sessionId;
    private String sessionName;
    private Long playerId;
    private String playerUsername;
    private String playerEmail;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private BigDecimal portfolioValue;
    private BigDecimal totalValue;
    private BigDecimal profitLoss;
    private Double profitLossPercentage;
    private Integer ranking;
    private Integer tradesCount;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActivityAt;
    private Boolean isActive;
}

