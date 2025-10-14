package tn.esprit.piboursebackend.Credit.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private double interestRate;
    private int durationMonths;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate repaymentDate;

    private BigDecimal totalToRepay;
    private String status; // PENDING, APPROVED, REPAID, LATE, REJECTED

    private int delayDays;
    private BigDecimal penaltyAmount;
    private double loanRiskScore;

    // 🔗 Relation : un prêt appartient à un joueur
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;*/

    // --- Méthodes métier ---
    public BigDecimal calculateTotalToRepay() {
        return amount.add(amount.multiply(BigDecimal.valueOf(interestRate / 100)));
    }

    public BigDecimal calculatePenalty() {
        if (delayDays > 0) {
            penaltyAmount = amount.multiply(BigDecimal.valueOf(0.01 * delayDays)); // 1% par jour de retard
        } else {
            penaltyAmount = BigDecimal.ZERO;
        }
        return penaltyAmount;
    }

    public boolean checkIfLate() {
        if (repaymentDate != null && repaymentDate.isAfter(dueDate)) {
            delayDays = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, repaymentDate);
            status = "LATE";
            return true;
        }
        return false;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}
