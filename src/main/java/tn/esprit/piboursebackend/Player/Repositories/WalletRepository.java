package tn.esprit.piboursebackend.Player.Repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.piboursebackend.Player.Entities.Wallet;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByPlayer_Id(Long playerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.player.id = :playerId")
    Optional<Wallet> findByPlayerIdForUpdate(Long playerId);

}
