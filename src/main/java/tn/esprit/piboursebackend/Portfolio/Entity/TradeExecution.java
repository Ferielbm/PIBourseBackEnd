package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Portfolio.Dto.Side;

import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name = "trade_execution",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_trade_external", columnNames = {"external_trade_id"})
        },
        indexes = {
                @Index(name = "ix_trade_portfolio_stock", columnList = "portfolio_id, stock_id"),
                @Index(name = "ix_trade_executed_at", columnList = "executed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_trade_id", length = 128, nullable = false)
    private String externalTradeId; // unique

    @Column(name = "external_order_id", length = 128)
    private String externalOrderId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Side side;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name="cash_flow_id")
    private Long cashFlowId;

    @Column(name = "cash_impact_base", precision = 20, scale = 6)
    private BigDecimal cashImpactBase;
}
