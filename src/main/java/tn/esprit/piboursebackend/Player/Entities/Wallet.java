package tn.esprit.piboursebackend.Player.Entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;  // Montant disponible actuel

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD"; // Devise par défaut

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    private BigDecimal totalDeposits = BigDecimal.ZERO; // Total des fonds reçus
    @Builder.Default
    private BigDecimal totalWithdrawals = BigDecimal.ZERO; // Total des retraits

    // ✅ Relation : chaque joueur possède un seul wallet 
    @OneToOne
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    @JsonBackReference
    private Player player;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (totalDeposits == null) {
            totalDeposits = BigDecimal.ZERO;
        }
        if (totalWithdrawals == null) {
            totalWithdrawals = BigDecimal.ZERO;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "USD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Méthodes utiles ---
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du dépôt doit être positif !");
        }
        balance = balance.add(amount);
        totalDeposits = totalDeposits.add(amount);
        updatedAt = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du retrait doit être positif !");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant ! Solde disponible: " + balance);
        }
        balance = balance.subtract(amount);
        totalWithdrawals = totalWithdrawals.add(amount);
        updatedAt = LocalDateTime.now();
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}