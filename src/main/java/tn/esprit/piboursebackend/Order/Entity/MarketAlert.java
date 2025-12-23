package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type d'alerte : achat massif / vente massive du Meneur
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MarketAlertType type;

    // Référence du titre (clé primaire de Stock)
    @Column(nullable = false, length = 20)
    private String stockSymbol;   // ex: "AAPL", "TSLA"

    // Quantité totale d’actions dans l’événement du Meneur
    @Column(precision = 19, scale = 4)
    private BigDecimal totalQuantity;

    // Valeur totale de l'opération (prix * quantité totale)
    @Column(precision = 19, scale = 4)
    private BigDecimal totalValue;

    // Message lisible par les joueurs
    @Column(nullable = false, length = 500)
    private String message;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}