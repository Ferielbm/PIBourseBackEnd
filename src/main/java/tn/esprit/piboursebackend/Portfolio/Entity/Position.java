package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.math.BigDecimal;
@Entity
@Table(name = "position",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_position_portfolio_stock", columnNames = {"portfolio_id", "stock_id"})
        },
        indexes = {
                @Index(name = "ix_position_stock", columnList = "stock_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long positionId;

    private Integer quantity;

    @Column(precision=20, scale=6)
    private BigDecimal averagePrice;

    @Transient
    private BigDecimal currentValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;
    @Column(nullable = false)
    private Integer reservedQuantity = 0;
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    @Version
    private Long version;
}
