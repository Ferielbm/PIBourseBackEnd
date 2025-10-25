package tn.esprit.piboursebackend.Credit.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Player.Entities.Player;
import java.math.BigDecimal;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;


    public void notifyLoanFullyRepaid(Player player, Loan loan) {
        String subject = "🎉 Félicitations ! Votre prêt a été totalement remboursé";
        String message = String.format(
                "Bonjour %s,\n\nVotre prêt de %.2f TND a été entièrement remboursé.\n" +
                        "Score de risque initial : %.2f\n\nMerci pour votre confiance.\nL’équipe PIBourse.",
                player.getUsername(),
                loan.getAmount(),
                loan.getLoanRiskScore()
        );

        emailService.sendEmail(player.getEmail(), subject, message);
    }

    public void notifyLoanPartialRepay(Player player, Loan loan, BigDecimal remaining) {
        String message = String.format(
                "💸 Vous avez remboursé partiellement votre prêt.\n" +
                        "Montant restant à payer : %.2f TND.", remaining
        );

        emailService.sendEmail(player.getEmail(), "Mise à jour de votre remboursement", message);
    }

    public void notifyGainAdded(Player player, BigDecimal gain) {
        String subject = "💰 Gain ajouté à votre wallet";
        String message = String.format(
                "Bonjour %s,\n\nUn gain de %.2f TND vient d’être ajouté à votre wallet.\n" +
                        "Merci pour votre fidélité.\nL’équipe PIBourse.",
                player.getUsername(),
                gain
        );

        emailService.sendEmail(player.getEmail(), subject, message);
    }
}
