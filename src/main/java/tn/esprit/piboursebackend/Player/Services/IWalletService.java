package tn.esprit.piboursebackend.Player.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.piboursebackend.Player.DTOs.WalletDTO;
import tn.esprit.piboursebackend.Player.DTOs.WalletTransactionDTO;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Entities.WalletTransaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IWalletService {
    
    /**
     * Create a new wallet for a player
     */
    Wallet createWalletForPlayer(Player player, BigDecimal initialBalance, String currency);
    
    /**
     * Get wallet by ID
     */
    WalletDTO getWalletById(Long walletId);
    
    /**
     * Get wallet by player ID
     */
    WalletDTO getWalletByPlayerId(Long playerId);
    
    /**
     * Get wallet entity by player ID (for internal use)
     */
    Wallet getWalletEntityByPlayerId(Long playerId);
    
    /**
     * Deposit funds into wallet
     */
    WalletTransactionDTO deposit(Long playerId, BigDecimal amount, String description);
    
    /**
     * Withdraw funds from wallet
     */
    WalletTransactionDTO withdraw(Long playerId, BigDecimal amount, String description);
    
    /**
     * Transfer funds between wallets
     */
    void transfer(Long fromPlayerId, Long toPlayerId, BigDecimal amount, String description);
    
    /**
     * Check if wallet has sufficient balance
     */
    boolean hasSufficientBalance(Long playerId, BigDecimal amount);
    
    /**
     * Get wallet balance
     */
    BigDecimal getBalance(Long playerId);
    
    /**
     * Get all transactions for a wallet
     */
    List<WalletTransactionDTO> getTransactionHistory(Long playerId);
    
    /**
     * Get paginated transactions for a wallet
     */
    Page<WalletTransactionDTO> getTransactionHistory(Long playerId, Pageable pageable);
    
    /**
     * Get transactions by type
     */
    List<WalletTransactionDTO> getTransactionsByType(Long playerId, TransactionType type);
    
    /**
     * Get transactions between dates
     */
    List<WalletTransactionDTO> getTransactionsBetweenDates(Long playerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Admin: Credit wallet (manual credit)
     */
    WalletTransactionDTO adminCredit(Long playerId, BigDecimal amount, String reason);
    
    /**
     * Admin: Debit wallet (manual debit)
     */
    WalletTransactionDTO adminDebit(Long playerId, BigDecimal amount, String reason);
    
    /**
     * Delete wallet (when player is deleted)
     */
    void deleteWallet(Long playerId);
}

