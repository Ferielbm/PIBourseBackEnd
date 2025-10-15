package tn.esprit.piboursebackend.Marche.Entity;

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
@Table(name = "order_books")
public class OrderBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal spread;

    @Column(precision = 15, scale = 2)
    private BigDecimal liquidity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_symbol", referencedColumnName = "symbol")
    private Stock stock;

    @Column(name = "total_bid_volume")
    private Integer totalBidVolume = 0;

    @Column(name = "total_ask_volume")
    private Integer totalAskVolume = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}