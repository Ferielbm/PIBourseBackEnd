package tn.esprit.piboursebackend.GameSession.Entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Player.Entities.Player;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "session_players", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"game_session_id", "player_id"}))
public class SessionPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal portfolioValue = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalValue = BigDecimal.ZERO;  // currentBalance + portfolioValue

    @Column
    private BigDecimal profitLoss;  // Gain/Perte par rapport au solde initial

    @Column
    private Double profitLossPercentage;  // % de gain/perte

    @Column
    private Integer ranking;  // Classement dans la session

    @Column
    private Integer tradesCount;  // Nombre de transactions effectuées

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime lastActivityAt;

    @Column
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (tradesCount == null) {
            tradesCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivityAt = LocalDateTime.now();
        calculateProfitLoss();
    }

    // Business methods

    public void calculateProfitLoss() {
        if (initialBalance != null && totalValue != null) {
            profitLoss = totalValue.subtract(initialBalance);
            if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
                profitLossPercentage = profitLoss
                    .divide(initialBalance, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            }
        }
    }

    public void updateTotalValue() {
        totalValue = currentBalance.add(portfolioValue);
        calculateProfitLoss();
    }

    public void incrementTradesCount() {
        if (tradesCount == null) {
            tradesCount = 0;
        }
        tradesCount++;
    }
}

