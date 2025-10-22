package tn.esprit.piboursebackend.Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="cash_balance")
@IdClass(CashBalance.PK.class)
public class CashBalance {
    @Id @ManyToOne @JoinColumn(name="portfolio_id")
    private Portfolio portfolio;

    @Id @Column(length=3)
    private String currency;

    @Column(nullable=false, precision=20, scale=6)
    private BigDecimal balance = BigDecimal.ZERO;

    @EqualsAndHashCode
    public static class PK implements java.io.Serializable {
        private Long portfolio; private String currency;
        public PK() {}
        public PK(Long p, String c){ this.portfolio=p; this.currency=c; }
    }
}
