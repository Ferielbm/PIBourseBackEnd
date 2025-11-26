package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
}
