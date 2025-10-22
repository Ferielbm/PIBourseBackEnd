package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.CashFlow;
import tn.esprit.piboursebackend.Portfolio.Entity.CashFlowType;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;

import java.time.Instant;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {
    boolean existsByPortfolio_IdAndRecordedAtAndAmountAndCurrencyAndType(
            Long portfolioId, Instant recordedAt, java.math.BigDecimal amount, String currency, CashFlowType type);

}
