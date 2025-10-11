package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Order;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal currentPrice;      // Dernier prix exécuté
    private BigDecimal spread;            // Écart entre offre et demande
    private BigDecimal liquidity;         // Indicateur global

    @OneToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

   /* @OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();*/
}
