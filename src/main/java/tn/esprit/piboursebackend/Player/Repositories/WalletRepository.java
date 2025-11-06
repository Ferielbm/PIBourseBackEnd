package tn.esprit.piboursebackend.Player.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    /**
     * Find wallet by associated player
     */
    Optional<Wallet> findByPlayer(Player player);
    
    /**
     * Find wallet by player ID
     */
    Optional<Wallet> findByPlayerId(Long playerId);
    
    /**
     * Check if a wallet exists for a player
     */
    boolean existsByPlayerId(Long playerId);
    
    /**
     * Delete wallet by player ID
     */
    void deleteByPlayerId(Long playerId);
}

