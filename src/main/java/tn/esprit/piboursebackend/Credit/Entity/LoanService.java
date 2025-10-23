package tn.esprit.piboursebackend.Credit.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CreditManager creditManager;
    private WalletRepository walletRepository;


    public Loan createLoan(Long playerId, BigDecimal amount) {
        // Ici, LoanService délègue la logique métier à CreditManager
        return creditManager.createLoan( playerId,  amount) ;
    }
    public void autoRepayLoan(Player player, BigDecimal gain) {

        // 1️⃣ Trouver le prêt actif
        Loan activeLoan = loanRepository.findByPlayerAndStatus(player, LoanStatus.APPROVED);

        if (activeLoan != null) {
            BigDecimal remaining = activeLoan.getRemainingAmount();

            // 2️⃣ Appliquer le gain pour rembourser le prêt
            if (gain.compareTo(remaining) >= 0) {
                // Gain suffit pour tout rembourser
                BigDecimal excess = gain.subtract(remaining);
                activeLoan.setRemainingAmount(BigDecimal.ZERO);
                activeLoan.setStatus(LoanStatus.REPAID);

                // Le reste du gain va au wallet du joueur
                Wallet wallet = player.getWallet();
                wallet.setBalance(wallet.getBalance().add(excess));
                walletRepository.save(wallet);

            } else {
                // Gain partiel → on diminue juste le reste du crédit
                activeLoan.setRemainingAmount(remaining.subtract(gain));
            }

            loanRepository.save(activeLoan);
        } else {
            // Aucun prêt actif → gain va directement dans le wallet
            Wallet wallet = player.getWallet();
            wallet.setBalance(wallet.getBalance().add(gain));
            walletRepository.save(wallet);
        }
    }

}
