package tn.esprit.piboursebackend.Marche.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@Table(name = "price_history")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "open_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal openPrice;

    @Column(name = "close_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal closePrice;

    @Column(name = "high_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal lowPrice;

    @Column(nullable = false)
    private Long volume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_symbol", referencedColumnName = "symbol")
    @JsonBackReference("stock-priceHistory")
    private Stock stock;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}