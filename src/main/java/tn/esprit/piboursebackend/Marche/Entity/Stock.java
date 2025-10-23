package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;            // Ex: AAPL
    private String companyName;       // Nom de l'entreprise
    private String sector;            // Ex: Technology
    private BigDecimal marketCap;         // Capitalisation boursière
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id")
    @JsonIgnoreProperties({"stocks", "hibernateLazyInitializer", "handler"})
    private Market market;
    
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"stock", "hibernateLazyInitializer", "handler"})
    private List<PriceHistory> priceHistoryList = new ArrayList<>();

   /* @OneToOne(mappedBy = "stock", cascade = CascadeType.ALL)
    private OrderBook orderBook;      // Lien vers le carnet d’ordres*/
}
