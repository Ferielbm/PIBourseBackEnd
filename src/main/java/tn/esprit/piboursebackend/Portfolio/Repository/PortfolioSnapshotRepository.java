package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot;

import java.time.Instant;
import java.util.List;

@Repository
    public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
        List<PortfolioSnapshot> findByPortfolioIdAndAsOfBetweenOrderByAsOfAsc(Long portfolioId, Instant start, Instant end);
        PortfolioSnapshot findFirstByPortfolioIdAndAsOfLessThanEqualOrderByAsOfDesc(Long portfolioId, Instant asOf);
        PortfolioSnapshot findFirstByPortfolioIdAndAsOfGreaterThanEqualOrderByAsOfAsc(Long portfolioId, Instant asOf);
    }

