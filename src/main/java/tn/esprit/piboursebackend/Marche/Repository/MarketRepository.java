package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Market;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    Optional<Market> findByCode(String code);
    Optional<Market> findByName(String name);

    @Query("SELECT COUNT(m) FROM Market m WHERE m.isOpen = true")
    Long countOpenMarkets();

    @Query("SELECT m FROM Market m WHERE m.isOpen = true")
    List<Market> findAllOpenMarkets();
}