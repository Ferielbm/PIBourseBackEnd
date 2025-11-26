package tn.esprit.piboursebackend.Portfolio.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByPortfolio_Id(Long portfolioId);
    Optional<Position> findByPortfolio_IdAndStock_Id(Long portfolioId, Long stockId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Position p where p.portfolio.id = :pid and p.stock.id = :sid")
    Optional<Position> findByPortfolio_IdAndStock_IdForUpdate(@Param("pid") Long portfolioId, @Param("sid") Long stockId);
}
