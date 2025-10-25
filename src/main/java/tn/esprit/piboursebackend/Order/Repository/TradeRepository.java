package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Trade;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findTop50ByStockOrderByExecutedAtDesc(Stock stock);
}
