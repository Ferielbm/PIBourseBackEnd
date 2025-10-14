package tn.esprit.piboursebackend.Credit.Entity;


import org.springframework.stereotype.Component;

@Component
public class CreditRiskEngine {

    // Calcule le score de risque d’un prêt
    /*public double calculateRiskScore(Player player, Loan loan) {
        // Exemple simple : plus la volatilité et le montant sont élevés, plus le risque est grand
        double base = (loan.getAmount().doubleValue() / (player.getCapital() + 1)) * 0.5;
        double volatilityFactor = player.getVolatility() * 0.3;
        double returnFactor = (1 - player.getWinRate()) * 0.2;
        double score = Math.min(1.0, base + volatilityFactor + returnFactor);
        loan.setLoanRiskScore(score);
        return score;
    }*/

    // Vérifie si un score est considéré comme risqué
    public boolean isHighRisk(double score) {
        return score > 0.7;
    }
}
