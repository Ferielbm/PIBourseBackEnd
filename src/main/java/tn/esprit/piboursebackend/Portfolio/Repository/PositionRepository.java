package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByPortfolio_Id(Long portfolioId);
    Optional<Position> findByPortfolio_IdAndStock_Id(Long portfolioId, Long stockId);
}
