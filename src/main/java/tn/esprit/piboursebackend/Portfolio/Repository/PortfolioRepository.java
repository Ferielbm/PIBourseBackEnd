package tn.esprit.piboursebackend.Portfolio.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Position p WHERE p.portfolio.id = :portfolioId AND p.stock.id = :stockId")
    Optional<Position> findByPortfolio_IdAndStock_IdForUpdate(@Param("portfolioId") Long portfolioId,
                                                              @Param("stockId") Long stockId);
}
