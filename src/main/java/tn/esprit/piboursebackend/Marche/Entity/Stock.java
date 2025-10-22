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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String symbol;             // e.g., "AAPL"

    @Column(nullable=false, length=3)
    private String currency;           // e.g., "USD", "TND"

    private String companyName;        // optional
    private String sector;             // optional

    @Column(precision=24, scale=6)
    private BigDecimal marketCap;      // optional

    @Column(precision=20, scale=6)
    private BigDecimal lastPrice;      // last known price in Stock.currency

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceHistory> priceHistoryList = new ArrayList<>();
}
