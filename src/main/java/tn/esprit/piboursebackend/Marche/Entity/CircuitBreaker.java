package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "circuit_breakers")
public class CircuitBreaker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drop_percentage", precision = 5, scale = 2)
    private BigDecimal dropPercentage;

    @Column(nullable = false)
    private String level;

    @Column(name = "pause_duration_minutes")
    private Integer pauseDurationMinutes;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(nullable = false)
    private Boolean active = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_symbol", referencedColumnName = "symbol")
    private Stock stock;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}