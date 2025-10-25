package tn.esprit.piboursebackend.Credit.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CreditManager creditManager;
    private WalletRepository walletRepository;
    private PlayerRepository playerRepository;
    @Autowired
    private NotificationService notificationService;


    public Loan createLoan(Long playerId, BigDecimal amount) {
        // Ici, LoanService délègue la logique métier à CreditManager
        return creditManager.createLoan( playerId,  amount) ;
    }
    public void autoRepayLoan(Player player, BigDecimal gain) {

        Loan activeLoan = loanRepository.findByPlayerAndStatus(player, LoanStatus.APPROVED);

        if (activeLoan != null) {
            BigDecimal remaining = activeLoan.getRemainingAmount();

            if (gain.compareTo(remaining) >= 0) {
                BigDecimal excess = gain.subtract(remaining);
                activeLoan.setRemainingAmount(BigDecimal.ZERO);
                activeLoan.setStatus(LoanStatus.REPAID);

                Wallet wallet = player.getWallet();
                wallet.setBalance(wallet.getBalance().add(excess));
                walletRepository.save(wallet);
                loanRepository.save(activeLoan);

                // ✅ Notifier l’utilisateur
                notificationService.notifyLoanFullyRepaid(player, activeLoan);

            } else {
                activeLoan.setRemainingAmount(remaining.subtract(gain));
                loanRepository.save(activeLoan);

                // ✅ Notifier remboursement partiel
                notificationService.notifyLoanPartialRepay(player, activeLoan, activeLoan.getRemainingAmount());
            }

        } else {
            Wallet wallet = player.getWallet();
            wallet.setBalance(wallet.getBalance().add(gain));
            walletRepository.save(wallet);

            // ✅ Notifier gain direct
            notificationService.notifyGainAdded(player, gain);
        }
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // 🔍 Récupérer les prêts d’un joueur

    public List<Loan> getLoansByPlayer(Long playerId) {
        return loanRepository.findByPlayer_Id(playerId);
    }
}
