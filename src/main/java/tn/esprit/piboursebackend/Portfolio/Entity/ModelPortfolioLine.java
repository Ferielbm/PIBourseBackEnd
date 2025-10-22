package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name="model_portfolio_line",
        uniqueConstraints = @UniqueConstraint(name="ux_mpl_model_stock", columnNames={"model_id","stock_id"}))
public class ModelPortfolioLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="model_id")
    private ModelPortfolio model;

    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="stock_id")
    private Stock stock;

    /** between 0 and 1 */
    @Column(nullable=false, precision=10, scale=6)
    private BigDecimal weight;
}