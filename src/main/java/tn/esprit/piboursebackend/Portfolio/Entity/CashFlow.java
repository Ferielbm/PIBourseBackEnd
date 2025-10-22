package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="cash_flow",
        indexes = {
                @Index(name="ix_cf_portfolio_asof", columnList="portfolio_id, as_of"),
        })
public class CashFlow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="portfolio_id")
    private Portfolio portfolio;

    @Column(name="as_of", nullable=false)
    private Instant  asOf;

    @Column(name="recorded_at", nullable=false, updatable=false)
    private Instant recordedAt;

    @Column(nullable=false, precision=20, scale=6)
    private BigDecimal amount;

    @Column(nullable=false, length=3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private CashFlowType type;
    @PrePersist void setRecordedAt(){ if (recordedAt==null) recordedAt = Instant.now(); }

}
