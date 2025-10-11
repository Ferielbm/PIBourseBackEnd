package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.Trade;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Dernières transactions d’un instrument (pour "last price", ticker, etc.)
    List<Trade> findTop50ByStockOrderByExecutedAtDesc(Stock stock);

    // Historique sur intervalle (graphique intraday/journalier)
    List<Trade> findByStockAndExecutedAtBetweenOrderByExecutedAtAsc(
            Stock stock, LocalDateTime from, LocalDateTime to);
}
