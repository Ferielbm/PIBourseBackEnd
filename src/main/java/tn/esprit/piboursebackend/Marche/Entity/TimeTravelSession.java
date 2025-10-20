package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "time_travel_sessions")
public class TimeTravelSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String playerId;

    @Column(name = "original_timeline_date")
    private LocalDateTime originalTimelineDate;

    @Column(name = "rewind_to_date", nullable = false)
    private LocalDateTime rewindToDate;

    @Column(name = "current_simulation_date")
    private LocalDateTime currentSimulationDate;

    @Enumerated(EnumType.STRING)
    private TimeTravelStatus status;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AlternativeTrade> alternativeTrades = new ArrayList<>();

    @Column(name = "original_portfolio_value", precision = 15, scale = 2)
    private java.math.BigDecimal originalPortfolioValue;

    @Column(name = "alternative_portfolio_value", precision = 15, scale = 2)
    private java.math.BigDecimal alternativePortfolioValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TimeTravelStatus {
        ACTIVE, COMPLETED, ABANDONED
    }
}