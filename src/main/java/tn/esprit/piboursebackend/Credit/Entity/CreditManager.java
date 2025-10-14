package tn.esprit.piboursebackend.Credit.Entity;


import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditManager {

    private final CreditRiskEngine riskEngine;

    public CreditManager(CreditRiskEngine riskEngine) {
        this.riskEngine = riskEngine;
    }

    /*public void evaluateLoan(Loan loan) {
        double score = riskEngine.calculateRiskScore(loan.getPlayer(), loan);
        if (riskEngine.isHighRisk(score)) {
            rejectLoan(loan);
        } else {
            approveLoan(loan);
        }
    }

    public void approveLoan(Loan loan) {
        loan.setStatus("APPROVED");
        System.out.println("Prêt approuvé pour " + loan.getPlayer().getUsername());
    }

    public void rejectLoan(Loan loan) {
        loan.setStatus("REJECTED");
        System.out.println("Prêt rejeté pour " + loan.getPlayer().getUsername());
    }

    public List<Loan> getLoanHistory(List<Loan> allLoans, Long playerId) {
        return allLoans.stream()
                .filter(l -> l.getPlayer().getId().equals(playerId))
                .collect(Collectors.toList());
    }*/
}