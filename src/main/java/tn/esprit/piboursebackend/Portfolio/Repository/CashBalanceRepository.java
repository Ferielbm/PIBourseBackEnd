package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.CashBalance;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CashBalanceRepository extends JpaRepository<CashBalance, CashBalance.PK> {

    @Modifying
    @Query("""
      update CashBalance cb
         set cb.balance = cb.balance + :delta
       where cb.portfolio.id = :portfolioId
         and cb.currency = :ccy
    """)
    int addToBalance(@Param("portfolioId") Long portfolioId,
                     @Param("ccy") String currency,
                     @Param("delta") BigDecimal delta);

    List<CashBalance> findByPortfolio_Id(Long portfolioId);
}
