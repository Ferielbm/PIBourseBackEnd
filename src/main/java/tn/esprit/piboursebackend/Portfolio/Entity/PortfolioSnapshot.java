package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="portfolio_snapshot",
        indexes = {@Index(name="ix_snap_portfolio_asof", columnList="portfolio_id, as_of")})
public class PortfolioSnapshot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="portfolio_id")
    private Portfolio portfolio;

    @Column(name="as_of", nullable=false)
    private Instant asOf;

    @Enumerated(EnumType.STRING)
    @Column(name="pricing_mode", nullable=false, length=24)
    private PricingMode pricingMode;

    @Column(name="nav", precision=20, scale=6, nullable=false)
    private BigDecimal nav;

    @Column(name="cash_base", precision=20, scale=6, nullable=false)
    private BigDecimal cashBase;

    @Column(name="mv_total", precision=20, scale=6, nullable=false)
    private BigDecimal marketValueTotal;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    // optional: store what prices/FX were used (audit/diagnostics)
    @Column(name="details_json", columnDefinition="json")
    private String detailsJson;

    @PrePersist void stamp(){ if (createdAt==null) createdAt = Instant.now(); }

    public enum PricingMode { MARK_TO_MARKET, MID, BID, ASK, FROZEN }
}
