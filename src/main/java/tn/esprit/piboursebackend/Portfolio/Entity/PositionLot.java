package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name="position_lot",
        indexes = @Index(name="ix_lot_position", columnList = "position_id"))
public class PositionLot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="position_id")
    private Position position;

    @Column(nullable=false)
    private Instant asOf;

    @Column(nullable=false)
    private Integer qtyOriginal;

    @Column(nullable=false)
    private Integer qtyRemaining;

    @Column(nullable=false, precision=20, scale=6)
    private BigDecimal price;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @PrePersist void stamp(){ if(createdAt==null) createdAt = Instant.now(); }
}
