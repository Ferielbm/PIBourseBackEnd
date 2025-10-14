package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime detectedAt;
    private String type;             // PUMP_AND_DUMP, SPOOFING, etc.
    private BigDecimal severity;         // Score d’anomalie
    private String description;      // Détails

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;
}
