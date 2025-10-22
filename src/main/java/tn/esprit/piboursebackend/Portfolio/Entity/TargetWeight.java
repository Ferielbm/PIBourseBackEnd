package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "target_weight",
        uniqueConstraints = @UniqueConstraint(name="ux_tw_portfolio_stock", columnNames = {"portfolio_id","stock_id"}))
public class TargetWeight {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="portfolio_id")
    private Portfolio portfolio;

    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="stock_id")
    private Stock stock;

    /** between 0 and 1 */
    @Column(nullable=false, precision=10, scale=6)
    private BigDecimal weight;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant updatedAt;

    @PrePersist void pre(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void touch(){ updatedAt = Instant.now(); }
}
