package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import javax.sound.midi.Instrument;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeInForce tif = TimeInForce.DAY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(precision = 19, scale = 6)
    private BigDecimal price;       // ignoré pour MARKET

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;    // quantité initiale

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal remainingQuantity; // quantité restante

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
