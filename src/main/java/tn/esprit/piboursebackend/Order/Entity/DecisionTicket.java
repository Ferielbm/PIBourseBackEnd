// src/main/java/tn/esprit/piboursebackend/Order/Entity/DecisionTicket.java
package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "decision_tickets",
        indexes = {
                @Index(name = "idx_ticket_player_status", columnList = "playerId,status"),
                @Index(name = "idx_ticket_symbol_time", columnList = "symbol,createdAt")
        })
public class DecisionTicket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private OrderSide side;           // BUY/SELL

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal foundPrice;    // prix trouvé sur le carnet

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal suggestedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketReason reason;      // IN_RANGE / APPROACHING_MIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DecisionStatus status;    // PENDING / ACCEPTED / REJECTED / EXPIRED

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime decidedAt;  // rempli quand accepté/rejeté
}
