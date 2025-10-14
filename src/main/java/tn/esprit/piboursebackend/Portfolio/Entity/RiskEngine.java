package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskEngine {
    @Id
    @GeneratedValue
    private int  riskEngineId;

    // === Domain methods ===
    public BigDecimal calculateVaR(Portfolio portfolio) {
        // Example logic (placeholder)
        return BigDecimal.valueOf(0.05).multiply(portfolio.getTotalValue());
    }

    public BigDecimal calculateSharpeRatio(Portfolio portfolio) {
        // Example logic
        return BigDecimal.ONE;
    }

 /*   public BigDecimal assessCreditRisk(Player player) {
        // Example logic
        return BigDecimal.ZERO;
    }*/

    public List<String> detectAnomalies() {
        // Example logic
        return List.of("No anomalies detected");
    }
}
