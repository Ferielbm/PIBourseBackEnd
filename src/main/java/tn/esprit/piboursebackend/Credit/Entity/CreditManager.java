package tn.esprit.piboursebackend.Credit.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;

@Service
public class CreditManager {
    @Autowired
    private final LoanRepository loanRepository;
    @Autowired
    private final CreditRiskEngine riskEngine;
    private final PlayerRepository playerRepository;
    private  final WalletRepository walletRepository;
    public CreditManager(LoanRepository loanRepository, CreditRiskEngine riskEngine, PlayerRepository playerRepository, WalletRepository walletRepository) {
        this.loanRepository = loanRepository;
        this.riskEngine = riskEngine;
        this.playerRepository = playerRepository;
        this.walletRepository = walletRepository;
    }

    public Loan createLoan(Long playerId, BigDecimal amount) {

        // 1️⃣ Vérifier si le joueur existe
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // 2️⃣ Vérifier les conditions d’éligibilité
        List<Loan> playerLoans = player.getLoans();

        // a. S’il a un crédit non remboursé → refuser la demande
        boolean hasActiveLoan = playerLoans.stream()
                .anyMatch(l -> l.getStatus() == LoanStatus.APPROVED);
        if (hasActiveLoan) {
            throw new RuntimeException("Vous avez déjà un crédit en cours de remboursement !");
        }

        // b. S’il a déjà eu 3 crédits au total → refuser
        if (player.getTotalCreditsTaken() >= 3) {
            throw new RuntimeException("Limite maximale de 3 crédits atteinte !");
        }

        // 3️⃣ Calcul du score de risque
        double riskScore = riskEngine.calculateRisk(player);

        // 4️⃣ Créer le prêt
        Loan loan = new Loan();
        loan.setPlayer(player);
        loan.setAmount(amount);
        loan.setRemainingAmount(amount);
        loan.setStartDate(LocalDate.now());
        loan.setLoanRiskScore(riskScore);

        // 5️⃣ Décision d’approbation automatique
        if (riskScore >= 0.7) {
            loan.setStatus(LoanStatus.REJECTED);
        } else {
            loan.setStatus(LoanStatus.APPROVED);

            // Ajouter le montant au wallet du joueur
            Wallet wallet = player.getWallet();
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);
        }

        player.setTotalCreditsTaken(player.getTotalCreditsTaken() + 1);
        playerRepository.save(player);
        // 6️⃣ Sauvegarde du prêt
        return loanRepository.save(loan);


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