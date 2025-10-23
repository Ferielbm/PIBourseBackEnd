package tn.esprit.piboursebackend.Credit.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Player.Entities.Player;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    // Compte le nombre total de crédits d’un joueur
    int countByPlayer(Player player);

    // Compte le nombre de crédits d’un joueur selon un statut (ex : REPAID, ACTIVE, DEFAULTED)
    int countByPlayerAndStatus(Player player, LoanStatus status);
    Loan findByPlayerAndStatus(Player player, LoanStatus status);
}
