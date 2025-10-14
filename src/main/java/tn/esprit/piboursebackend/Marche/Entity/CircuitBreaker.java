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
public class CircuitBreaker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal dropPercentage;        // Ex: -7%, -13%, -20%
    private String level;                 // Niveau 1, 2, 3
    private Integer pauseDurationMinutes; // Durée de la suspension
    private LocalDateTime triggeredAt;    // Date de déclenchement
    private Boolean active;               // True si encore en pause

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;
}
