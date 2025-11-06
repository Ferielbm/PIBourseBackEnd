package tn.esprit.piboursebackend.Player.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Player.Entities.WalletTransaction;
import tn.esprit.piboursebackend.Player.Entities.WalletTransaction.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    /**
     * Find all transactions for a specific wallet
     */
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    /**
     * Find all transactions for a specific wallet with pagination
     */
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    /**
     * Find transactions by wallet and type
     */
    List<WalletTransaction> findByWalletIdAndTypeOrderByCreatedAtDesc(Long walletId, TransactionType type);
    
    /**
     * Find transactions by wallet between dates
     */
    List<WalletTransaction> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long walletId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find transaction by reference
     */
    Optional<WalletTransaction> findByReference(String reference);
    
    /**
     * Count transactions by wallet
     */
    long countByWalletId(Long walletId);
    
    /**
     * Get recent transactions for a wallet
     */
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.id = :walletId ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findRecentTransactions(Long walletId, Pageable pageable);
}

