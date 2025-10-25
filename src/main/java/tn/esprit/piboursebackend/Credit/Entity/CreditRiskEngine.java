package tn.esprit.piboursebackend.Credit.Entity;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Credit.Entity.LoanRepository;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CreditRiskEngine {

    private final LoanRepository loanRepository;


    public double calculateRisk(Player player) {
        double baseRisk = 0.5; // Risque neutre par défaut

        /*// ✅ 1. Crédits déjà remboursés → réduit le risque
        int repaidCredits = loanRepository.countByPlayerAndStatus(player, LoanStatus.REPAID);
        if (repaidCredits > 0) {
            baseRisk -= repaidCredits * 0.1; // -0.1 de risque par crédit remboursé
        }*/

        // ✅ 2. Solde du wallet → plus le joueur a du capital, moins il est risqué
        if (player.getWallet() != null && player.getWallet().getBalance().compareTo(BigDecimal.ZERO) > 0) {
            baseRisk -= 0.1;
        }

        // ✅ 3. Analyse de tous les portfolios du joueur
        if (player.getPortfolios() != null && !player.getPortfolios().isEmpty()) {
            BigDecimal totalPnL = BigDecimal.ZERO;

            for (Portfolio portfolio : player.getPortfolios()) {
                BigDecimal realized = portfolio.getRealizedPnL() != null ? portfolio.getRealizedPnL() : BigDecimal.ZERO;
                BigDecimal unrealized = portfolio.getUnrealizedPnL() != null ? portfolio.getUnrealizedPnL() : BigDecimal.ZERO;
                totalPnL = totalPnL.add(realized.add(unrealized));
            }

            // Si pertes totales sur les portefeuilles → risque augmente
            if (totalPnL.compareTo(BigDecimal.ZERO) < 0) {
                baseRisk += 0.2;
            }
        }

        // ✅ 4. Score final borné entre 0 et 1
        return Math.min(1.0, Math.max(0.0, baseRisk));
    }
}
