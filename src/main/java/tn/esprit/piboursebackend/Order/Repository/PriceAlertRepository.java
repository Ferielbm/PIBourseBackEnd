// tn/esprit/piboursebackend/Order/Repository/PriceAlertRepository.java
package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Order.Entity.PriceAlert;
import tn.esprit.piboursebackend.Order.Entity.PriceAlertStatus;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    List<PriceAlert> findByPlayerIdAndStatusOrderByCreatedAtDesc(Long playerId, PriceAlertStatus status);


}
