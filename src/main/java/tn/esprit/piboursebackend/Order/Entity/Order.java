package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import javax.sound.midi.Instrument;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
@Entity
@Table(name = "`order`")  // Échapper "order" car c'est un mot réservé SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 // Optionnel mais recommandé : évite les écrasements concurrents
 @Version
 private Long version;

 @ManyToOne(optional = false, fetch = FetchType.LAZY)
 @JoinColumn(name = "stock_id", nullable = false)
 private Stock stock;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 10)
 private OrderType type; // MARKET / LIMIT

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 5)
 private OrderSide side; // BUY / SELL

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 10)
 @Builder.Default
 private TimeInForce tif = TimeInForce.DAY; // DAY / GTC / IOC / FOK (selon ton enum)

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 20)
 @Builder.Default
 private OrderStatus status = OrderStatus.PENDING;

 // Prix ignoré pour MARKET ; requis pour LIMIT (à valider côté service)
 @Column(precision = 19, scale = 6)
 private BigDecimal price;

 // Quantité initiale
 @Column(nullable = false, precision = 19, scale = 6)
 private BigDecimal quantity;

 // Quantité restante (initialisée = quantity)
 @Column(nullable = false, precision = 19, scale = 6)
 private BigDecimal remainingQuantity;

 @Column(nullable = false, updatable = false)
 private LocalDateTime createdAt;

 @Column
 private LocalDateTime updatedAt;

 @PrePersist
 public void prePersist() {
  // Timestamps
  if (createdAt == null) createdAt = LocalDateTime.now();
  updatedAt = createdAt;

  // Défauts métier
  if (status == null) status = OrderStatus.PENDING;
  if (tif == null) tif = TimeInForce.DAY;

  // Normalisation des décimales
  quantity = scale(quantity);
  if (remainingQuantity == null) remainingQuantity = quantity;
  remainingQuantity = scale(remainingQuantity);
  if (price != null) price = scale(price);

  // (Optionnel) Si MARKET, on s'assure que price est null
  if (type == OrderType.MARKET) {
   price = null;
  }
 }

 @PreUpdate
 public void preUpdate() {
  updatedAt = LocalDateTime.now();

  // Normalisation des décimales
  if (quantity != null) quantity = scale(quantity);
  if (remainingQuantity != null) remainingQuantity = scale(remainingQuantity);
  if (price != null) price = scale(price);
 }

 private BigDecimal scale(BigDecimal v) {
  return (v == null) ? null : v.setScale(6, RoundingMode.HALF_UP);
 }
}