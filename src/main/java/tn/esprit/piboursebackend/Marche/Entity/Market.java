package tn.esprit.piboursebackend.Marche.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "markets")
public class Market {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_date")
    private LocalDateTime currentDate;    // Date courante simulée
    private Boolean isOpen;               // Marché ouvert/fermé
    private BigDecimal timeCompressionRatio;  // Ex: 1h réelle = 1 semaine simulée

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "market_id")
    private List<Stock> stocks = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDateTime currentDate) {
        this.currentDate = currentDate;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public BigDecimal getTimeCompressionRatio() {
        return timeCompressionRatio;
    }

    public void setTimeCompressionRatio(BigDecimal timeCompressionRatio) {
        this.timeCompressionRatio = timeCompressionRatio;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}
