package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.ModelPortfolioLine;

import java.util.List;

@Repository
public interface ModelPortfolioLineRepository extends JpaRepository<ModelPortfolioLine, Long> {
    List<ModelPortfolioLine> findByModel_Id(Long modelId);
}