package tn.esprit.piboursebackend.Order.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long playerId;
    private Long stockId;
    private OrderType type;
    private OrderSide side;
    private TimeInForce tif;
    private OrderStatus status;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal remainingQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relations simplifiées
    private StockInfo stock;
    private PlayerInfo player;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockInfo {
        private Long id;
        private String symbol;
        private String name;
        private BigDecimal lastPrice;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerInfo {
        private Long id;
        private String username;
    }
}
