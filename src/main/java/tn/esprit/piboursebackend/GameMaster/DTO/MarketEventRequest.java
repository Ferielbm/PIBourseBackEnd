package tn.esprit.piboursebackend.GameMaster.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketEventRequest {
    private String eventType; // "CRASH", "BULL_RUN", "VOLATILITY", "MANIPULATION"
    private String symbol; // Optionnel, pour cibler un symbole spécifique
    private Integer intensity; // 1-100
}
