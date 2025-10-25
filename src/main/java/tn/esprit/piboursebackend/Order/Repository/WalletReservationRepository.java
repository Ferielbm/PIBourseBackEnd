package tn.esprit.piboursebackend.Order.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Order.Entity.WalletReservation;
import tn.esprit.piboursebackend.Order.Entity.WalletReservationStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletReservationRepository extends JpaRepository<WalletReservation, Long> {

    @Query("select coalesce(sum(r.remainingAmount),0) from WalletReservation r " +
            "where r.playerId = :playerId and r.status = tn.esprit.piboursebackend.Order.Entity.WalletReservationStatus.ACTIVE")
    BigDecimal sumActiveRemainingByPlayerId(Long playerId);

    List<WalletReservation> findByPlayerIdAndStatus(Long playerId, WalletReservationStatus status);

    Optional<WalletReservation> findFirstByOrderIdAndStatus(Long orderId, WalletReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from WalletReservation r where r.orderId = :orderId and r.status = tn.esprit.piboursebackend.Order.Entity.WalletReservationStatus.ACTIVE")
    List<WalletReservation> lockAllActiveByOrderId(Long orderId);
}
