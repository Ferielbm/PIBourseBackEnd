package tn.esprit.piboursebackend.Player.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal balanceBefore;

    @Column(nullable = false)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String reference; // Reference unique de la transaction (pour tracking)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        DEPOSIT,         // Dépôt de fonds
        WITHDRAWAL,      // Retrait de fonds
        STOCK_PURCHASE,  // Achat d'actions
        STOCK_SALE,      // Vente d'actions
        ADMIN_CREDIT,    // Crédit administratif
        ADMIN_DEBIT,     // Débit administratif
        TRANSFER_IN,     // Transfert entrant
        TRANSFER_OUT,    // Transfert sortant
        BONUS,           // Bonus
        PENALTY          // Pénalité
    }
}

