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

    private BigDecimal dropPercentage;
    private String level;
    private Integer pauseDurationMinutes;
    private LocalDateTime triggeredAt;
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;
}
