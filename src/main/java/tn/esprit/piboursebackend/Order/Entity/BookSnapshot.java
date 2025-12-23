package tn.esprit.piboursebackend.Order.Entity;

import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookSnapshot {
    // Exemple: { "10.50": 1200, "10.45": 800 }
    private Map<String, Long> bids;
    private Map<String, Long> asks;
    private Double lastPrice; // number dans l’UI
}
