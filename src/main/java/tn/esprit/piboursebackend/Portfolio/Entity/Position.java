package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.math.BigDecimal;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    @Id
    @GeneratedValue
    private Long positionId;

    private Integer quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentValue;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Stock stock;

    // ✅ Correct: each Position belongs to ONE Portfolio (not a list)
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    public BigDecimal calculateProfitLoss(BigDecimal currentPrice) {
        return currentPrice.subtract(averagePrice)
                .multiply(BigDecimal.valueOf(quantity));
    }
}