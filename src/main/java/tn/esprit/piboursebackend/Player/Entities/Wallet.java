package tn.esprit.piboursebackend.Player.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal balance = BigDecimal.ZERO;  // Montant disponible actuel

    private BigDecimal totalDeposits = BigDecimal.ZERO; // Total des fonds reçus
    private BigDecimal totalWithdrawals = BigDecimal.ZERO; // Total des retraits

    // ✅ Relation : chaque joueur possède un seul wallet
    @OneToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // --- Méthodes utiles ---
    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
        totalDeposits = totalDeposits.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) >= 0) {
            balance = balance.subtract(amount);
            totalWithdrawals = totalWithdrawals.add(amount);
        } else {
            throw new IllegalArgumentException("Solde insuffisant !");
        }
    }
}