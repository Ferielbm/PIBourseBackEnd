package tn.esprit.piboursebackend.GameMaster.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FictiveOrderRequest {
    private String symbol;
    private String side; // "BUY" ou "SELL"
    private Long quantity;
    private String priceType; // "MARKET" ou "LIMIT"
    private Double limitPrice;
    private String description;
}
