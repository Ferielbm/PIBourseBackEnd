package tn.esprit.piboursebackend.Order.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MarketAlertDto {
    private String type;        // BULLISH_MJ_BUY / BEARISH_MJ_SELL
    private String stockSymbol;
    private String message;
    private LocalDateTime createdAt;
}
