// src/main/java/tn/esprit/piboursebackend/Order/Entity/ScheduledOrder.java
package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "scheduled_orders",
        indexes = {
                @Index(name="idx_scheduled_player_status", columnList="player_id,status"),
                @Index(name="idx_scheduled_symbol_status", columnList="desired_symbol,status")
        })
public class ScheduledOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 160)
    private String actor; // "user:{playerId}" ou "api"

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    // 🔴 IMPORTANT : ce champ doit exister car le repo l’utilise
    @Column(name = "desired_symbol", nullable = false, length = 20)
    private String desiredSymbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private OrderSide side; // BUY/SELL

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "min_price", precision = 19, scale = 6)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 19, scale = 6)
    private BigDecimal maxPrice;

    @Column(name = "notify_only", nullable = false)
    private boolean notifyOnly;

    @Column(name = "notify_when_approach_min", nullable = false)
    private boolean notifyWhenApproachMin;

    @Column(name = "approach_threshold_pct", precision = 8, scale = 4)
    private BigDecimal approachThresholdPct;

    @Column(name = "approach_cooldown_minutes", nullable = false)
    private Integer approachCooldownMinutes;

    @Column(name = "last_approach_notified_at")
    private LocalDateTime lastApproachNotifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduledOrderStatus status; // PENDING / TRIGGERED / CANCELLED / FAILED

    // champs optionnels utilisés lors du trigger
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private OrderType type; // MARKET/LIMIT (souvent LIMIT pour le trigger)

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TimeInForce tif; // DAY/GTC/IOC/FOK

    @Column(precision = 19, scale = 6)
    private BigDecimal price; // prix utilisé au trigger (si on enregistre)

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @PrePersist
    void onCreate(){
        if (approachCooldownMinutes == null) approachCooldownMinutes = 60;
        if (status == null) status = ScheduledOrderStatus.PENDING;
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
