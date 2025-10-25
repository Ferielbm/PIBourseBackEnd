// tn/esprit/piboursebackend/Order/Entity/WalletReservation.java
package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "wallet_reservations",
        indexes = {
                @Index(name = "idx_wr_player_status", columnList = "playerId,status"),
                @Index(name = "idx_wr_order_status", columnList = "orderId,status")
        })
public class WalletReservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    // ⚠️ On stocke directement l'id du player pour éviter une dépendance forte au module Player
    @Column(nullable = false)
    private Long playerId;

    // L’ordre pour lequel on réserve
    @Column(nullable = false)
    private Long orderId;

    // Montant réservé initial et montant restant consommable
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal amountReserved;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private WalletReservationStatus status = WalletReservationStatus.ACTIVE;

    @Column(length = 120)
    private String reason; // ex: BUY_ORDER_RESERVE

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
