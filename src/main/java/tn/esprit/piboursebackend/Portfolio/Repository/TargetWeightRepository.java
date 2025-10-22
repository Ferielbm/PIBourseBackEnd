package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetWeightRepository extends JpaRepository<TargetWeight, Long> {
    List<TargetWeight> findByPortfolio_Id(Long portfolioId);
    Optional<TargetWeight> findByPortfolio_IdAndStock_Id(Long portfolioId, Long stockId);
    void deleteByPortfolio_Id(Long portfolioId);
}