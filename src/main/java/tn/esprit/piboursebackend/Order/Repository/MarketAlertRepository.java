package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Order.Entity.MarketAlert;

import java.util.List;

public interface MarketAlertRepository extends JpaRepository<MarketAlert, Long>  {
    List<MarketAlert> findByStockSymbolOrderByCreatedAtDesc(String stockSymbol);

}
