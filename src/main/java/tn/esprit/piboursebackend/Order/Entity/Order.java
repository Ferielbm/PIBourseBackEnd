package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import javax.sound.midi.Instrument;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

 private static final int SCALE = 6;

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Version
 private Long version;

 @ManyToOne(optional = false, fetch = FetchType.LAZY)
 @JoinColumn(name = "stock_id", nullable = false)
 private Stock stock;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 10)
 private OrderType type;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 5)
 private OrderSide side;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 10)
 @Builder.Default
 private TimeInForce tif = TimeInForce.DAY;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 20)
 @Builder.Default
 private OrderStatus status = OrderStatus.PENDING;

 @Column(precision = 19, scale = 6)
 private BigDecimal price;

 @Column(nullable = false, precision = 19, scale = 6)
 private BigDecimal quantity;

 @Column(nullable = false, precision = 19, scale = 6)
 private BigDecimal remainingQuantity;

 @CreationTimestamp
 @Column(nullable = false, updatable = false)
 private LocalDateTime createdAt;

 @UpdateTimestamp
 @Column(nullable = false)
 private LocalDateTime updatedAt;

 @PrePersist
 public void prePersist() {
  if (status == null) status = OrderStatus.PENDING;
  if (tif == null) tif = TimeInForce.DAY;

  quantity = scale(quantity);
  if (remainingQuantity == null) remainingQuantity = quantity;
  remainingQuantity = scale(remainingQuantity);
  if (price != null) price = scale(price);

  if (type == OrderType.MARKET) {
   price = null;
  }
 }

 @PreUpdate
 public void preUpdate() {
  if (quantity != null) quantity = scale(quantity);
  if (remainingQuantity != null) remainingQuantity = scale(remainingQuantity);
  if (price != null) price = scale(price);
 }

 private BigDecimal scale(BigDecimal v) {
  return (v == null) ? null : v.setScale(SCALE, RoundingMode.HALF_UP);
 }

 public boolean isOpen() {
  return status == OrderStatus.PENDING || status == OrderStatus.PARTIALLY_FILLED;
 }
}