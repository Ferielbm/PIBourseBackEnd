// tn/esprit/piboursebackend/Order/Entity/PriceAlert.java
package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "price_alerts",
        indexes = {
                @Index(name="idx_alert_player_symbol", columnList = "playerId,symbol"),
                @Index(name="idx_alert_symbol_status", columnList = "symbol,status")
        })
public class PriceAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private Long version;

    @Column(nullable = false) private Long playerId;        // ton "player" en base
    @Column(nullable = false, length = 20) private String symbol;

    @Column(nullable = false, precision = 19, scale = 6) private BigDecimal minPrice;
    @Column(nullable = false, precision = 19, scale = 6) private BigDecimal maxPrice;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private PriceAlertStatus status;

    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = PriceAlertStatus.ACTIVE;
    }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
