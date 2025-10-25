// src/main/java/tn/esprit/piboursebackend/Order/Repository/ScheduledOrderRepository.java
package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Order.Entity.ScheduledOrder;
import tn.esprit.piboursebackend.Order.Entity.ScheduledOrderStatus;

import java.util.List;

public interface ScheduledOrderRepository extends JpaRepository<ScheduledOrder, Long> {

    List<ScheduledOrder> findByPlayerIdAndStatusOrderByCreatedAtAsc(Long playerId, ScheduledOrderStatus status);

    List<ScheduledOrder> findByPlayerIdAndStatusAndDesiredSymbolOrderByCreatedAtAsc(
            Long playerId, ScheduledOrderStatus status, String desiredSymbol
    );


    // utile si tu filtres seulement par symbole + status ailleurs
    List<ScheduledOrder> findByDesiredSymbolAndStatus(String desiredSymbol, ScheduledOrderStatus status);
}
