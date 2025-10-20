package tn.esprit.piboursebackend.Marche.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stocks")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "symbol")
public class Stock {
    @Id
    @Column(name = "symbol", unique = true, nullable = false)
    private String symbol; // AAPL, TSLA, etc.

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String sector;

    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_code", referencedColumnName = "code")
    @JsonBackReference("market-stocks")
    private Market market;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("stock-priceHistory")
    private List<PriceHistory> priceHistoryList = new ArrayList<>();

    @OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("stock-orderBook")
    private OrderBook orderBook;



    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}