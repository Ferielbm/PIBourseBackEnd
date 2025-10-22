package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.ModelPortfolio;

@Repository
public interface ModelPortfolioRepository extends JpaRepository<ModelPortfolio, Long> {}
