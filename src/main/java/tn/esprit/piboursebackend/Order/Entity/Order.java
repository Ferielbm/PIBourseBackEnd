package tn.esprit.piboursebackend.Order.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_order_stock_side_price_created", columnList = "stock_id,side,price,createdAt"),
                @Index(name = "idx_order_stock_status", columnList = "stock_id,status")
        })
public class Order {

 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Version
 private Long version;

 @ManyToOne(optional = false, fetch = FetchType.LAZY)
 @JoinColumn(name = "stock_id", nullable = false)
 @JsonIgnore
 private Stock stock;

 @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
 private OrderType type; // MARKET / LIMIT

 @Enumerated(EnumType.STRING) @Column(nullable = false, length = 5)
 private OrderSide side; // BUY / SELL

 @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
 @Builder.Default
 private TimeInForce tif = TimeInForce.DAY; // DAY/GTC/IOC/FOK

 @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
 @Builder.Default
 private OrderStatus status = OrderStatus.PENDING;

 @Column(precision = 19, scale = 6)
 private BigDecimal price; // null pour MARKET

 @Column(nullable = false, precision = 19, scale = 6)
 private BigDecimal quantity;

 @Column(nullable = false, precision = 19, scale = 6)
 private BigDecimal remainingQuantity;

 @Column(nullable = false, updatable = false)
 private LocalDateTime createdAt;

 @Column
 private LocalDateTime updatedAt;

 @PrePersist
 void onCreate() {
  if (createdAt == null) createdAt = LocalDateTime.now();
  updatedAt = createdAt;
  if (status == null) status = OrderStatus.PENDING;
  if (tif == null) tif = TimeInForce.DAY;

  quantity = scale(quantity);
  if (remainingQuantity == null) remainingQuantity = quantity;
  remainingQuantity = scale(remainingQuantity);
  if (price != null) price = scale(price);

  if (type == OrderType.MARKET) price = null;
 }

 @PreUpdate
 void onUpdate() {
  updatedAt = LocalDateTime.now();
  if (quantity != null) quantity = scale(quantity);
  if (remainingQuantity != null) remainingQuantity = scale(remainingQuantity);
  if (price != null) price = scale(price);
 }

 private BigDecimal scale(BigDecimal v) {
  return (v == null) ? null : v.setScale(6, RoundingMode.HALF_UP);
 }
}
