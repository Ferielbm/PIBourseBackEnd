package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alternative_trades")
public class AlternativeTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    private TradeAction action;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal quantity;

    @Column(name = "execution_price", precision = 10, scale = 2)
    private java.math.BigDecimal executionPrice;

    @Column(name = "execution_date")
    private LocalDateTime executionDate;

    @Column(name = "original_decision") // Ce qui a été réellement fait
    private String originalDecision;

    @Column(name = "alternative_decision") // Ce que le joueur change
    private String alternativeDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TimeTravelSession session;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TradeAction {
        BUY, SELL, HOLD
    }
}