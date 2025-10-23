package tn.esprit.piboursebackend.Credit.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Player.Entities.Player;

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
    private Long loanId;

    private BigDecimal amount;

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public double getLoanRiskScore() {
        return loanRiskScore;
    }

    public void setLoanRiskScore(double loanRiskScore) {
        this.loanRiskScore = loanRiskScore;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private LocalDate startDate;
    private LoanStatus status;
    private double loanRiskScore;
    private BigDecimal remainingAmount; // Montant restant à rembourser
    private LocalDate endDate; // optionnel, juste pour historique


    // 🔗 Relation : un prêt appartient à un joueur (désactivée pour l’instant)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;


}