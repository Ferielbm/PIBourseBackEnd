package tn.esprit.piboursebackend.GameMaster.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketStatsResponse {
    private Integer totalOrders;
    private Long totalVolume;
    private Integer activePlayers;
    private String mostTradedSymbol;
}
