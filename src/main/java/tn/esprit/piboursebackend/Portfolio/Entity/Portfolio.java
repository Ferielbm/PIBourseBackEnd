package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Player.Entities.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {
    @Id
    @GeneratedValue
    private Long portfolioId;

    private BigDecimal totalValue;
    private BigDecimal unrealizedPnL;
    private BigDecimal realizedPnL;

    // ✅ Correct: a Portfolio belongs to ONE Player
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;


    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Stock stock;
    // ✅ Correct: One portfolio can have many positions
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    public BigDecimal calculateTotalValue() {
        return positions.stream()
                .map(Position::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addPosition(Position position) {
        positions.add(position);
        position.setPortfolio(this);
    }

    public void removePosition(Position position) {
        positions.remove(position);
        position.setPortfolio(null);
    }
}
