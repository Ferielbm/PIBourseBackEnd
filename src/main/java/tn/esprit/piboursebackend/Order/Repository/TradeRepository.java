package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Trade;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findTop50ByStockOrderByExecutedAtDesc(Stock stock);

    List<Trade> findByStockAndExecutedAtBetweenOrderByExecutedAtAsc(
            Stock stock, LocalDateTime from, LocalDateTime to);
}
