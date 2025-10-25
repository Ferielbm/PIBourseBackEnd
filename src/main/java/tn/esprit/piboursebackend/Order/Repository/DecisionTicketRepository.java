// tn/esprit/piboursebackend/Order/Repository/DecisionTicketRepository.java
package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Order.Entity.DecisionTicket;
import tn.esprit.piboursebackend.Order.Entity.DecisionStatus;

import java.util.List;

public interface DecisionTicketRepository extends JpaRepository<DecisionTicket, Long> {
    List<DecisionTicket> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    // Tickets d’un player par statut (PENDING/ACCEPTED/REJECTED) — nécessite createdAt dans l’entité
    List<DecisionTicket> findByPlayerIdAndStatusOrderByCreatedAtDesc(Long playerId, DecisionStatus status);
    List<DecisionTicket> findByPlayerIdAndStatus(Long playerId, DecisionStatus status);

// Si ton entité n’a PAS de createdAt, utilise ces deux signatures à la place :
// List<DecisionTicket> findByPlayerId(Long playerId);
// List<DecisionTicket> findByPlayerIdAndStatus(Long playerId, DecisionStatus status);
}
