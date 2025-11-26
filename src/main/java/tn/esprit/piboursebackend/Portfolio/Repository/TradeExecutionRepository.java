package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Portfolio.Entity.TradeExecution;

import java.util.Optional;

public interface TradeExecutionRepository extends JpaRepository<TradeExecution, Long> {

    boolean existsByExternalTradeId(String externalTradeId);

    Optional<TradeExecution> findByExternalTradeId(String externalTradeId);
}