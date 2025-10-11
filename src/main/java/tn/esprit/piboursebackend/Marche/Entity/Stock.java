package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;            // Ex: AAPL
    private String companyName;       // Nom de l’entreprise
    private String sector;            // Ex: Technology
    private BigDecimal marketCap;         // Capitalisation boursière
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Position position ;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Portfolio portfolio;
   @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)

    private List<PriceHistory> priceHistoryList = new ArrayList<>();

   /* @OneToOne(mappedBy = "stock", cascade = CascadeType.ALL)
    private OrderBook orderBook;      // Lien vers le carnet d’ordres*/
}
