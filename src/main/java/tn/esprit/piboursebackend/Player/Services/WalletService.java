package tn.esprit.piboursebackend.Player.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Player.DTOs.WalletDTO;
import tn.esprit.piboursebackend.Player.DTOs.WalletTransactionDTO;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Entities.WalletTransaction;
import tn.esprit.piboursebackend.Player.Entities.WalletTransaction.TransactionType;
import tn.esprit.piboursebackend.Player.Exceptions.ResourceNotFoundException;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletService implements IWalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Value("${wallet.default.currency:USD}")
    private String defaultCurrency;

    @Value("${wallet.initial.balance:10000.00}")
    private BigDecimal defaultInitialBalance;

    public WalletService(WalletRepository walletRepository,
                         WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Wallet createWalletForPlayer(Player player, BigDecimal initialBalance, String currency) {
        logger.info("Creating wallet for player: {}", player.getEmail());

        // Check if wallet already exists
        if (walletRepository.existsByPlayerId(player.getId())) {
            throw new IllegalArgumentException("Le joueur possède déjà un portefeuille");
        }

        // Create wallet
        Wallet wallet = Wallet.builder()
                .player(player)
                .balance(initialBalance != null ? initialBalance : defaultInitialBalance)
                .currency(currency != null ? currency : defaultCurrency)
                .totalDeposits(initialBalance != null ? initialBalance : defaultInitialBalance)
                .totalWithdrawals(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        // Create initial transaction if initial balance > 0
        if (savedWallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            createTransaction(
                    savedWallet,
                    TransactionType.ADMIN_CREDIT,
                    savedWallet.getBalance(),
                    BigDecimal.ZERO,
                    savedWallet.getBalance(),
                    "Solde initial du compte",
                    generateReference()
            );
        }

        logger.info("Wallet created successfully for player: {}", player.getEmail());
        return savedWallet;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletDTO getWalletById(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Portefeuille non trouvé avec l'ID: " + walletId));
        return convertToDTO(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletDTO getWalletByPlayerId(Long playerId) {
        Wallet wallet = walletRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Portefeuille non trouvé pour le joueur ID: " + playerId));
        return convertToDTO(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getWalletEntityByPlayerId(Long playerId) {
        return walletRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Portefeuille non trouvé pour le joueur ID: " + playerId));
    }

    @Override
    public WalletTransactionDTO deposit(Long playerId, BigDecimal amount, String description) {
        logger.info("Depositing {} to player {}", amount, playerId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du dépôt doit être positif");
        }

        Wallet wallet = getWalletEntityByPlayerId(playerId);
        BigDecimal balanceBefore = wallet.getBalance();

        wallet.deposit(amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                wallet,
                TransactionType.DEPOSIT,
                amount,
                balanceBefore,
                wallet.getBalance(),
                description != null ? description : "Dépôt de fonds",
                generateReference()
        );

        logger.info("Deposit successful. New balance: {}", wallet.getBalance());
        return convertTransactionToDTO(transaction);
    }

    @Override
    public WalletTransactionDTO withdraw(Long playerId, BigDecimal amount, String description) {
        logger.info("Withdrawing {} from player {}", amount, playerId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du retrait doit être positif");
        }

        Wallet wallet = getWalletEntityByPlayerId(playerId);
        BigDecimal balanceBefore = wallet.getBalance();

        if (!wallet.hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Solde insuffisant. Disponible: " + wallet.getBalance());
        }

        wallet.withdraw(amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                wallet,
                TransactionType.WITHDRAWAL,
                amount,
                balanceBefore,
                wallet.getBalance(),
                description != null ? description : "Retrait de fonds",
                generateReference()
        );

        logger.info("Withdrawal successful. New balance: {}", wallet.getBalance());
        return convertTransactionToDTO(transaction);
    }

    @Override
    public void transfer(Long fromPlayerId, Long toPlayerId, BigDecimal amount, String description) {
        logger.info("Transferring {} from player {} to player {}", amount, fromPlayerId, toPlayerId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du transfert doit être positif");
        }

        if (fromPlayerId.equals(toPlayerId)) {
            throw new IllegalArgumentException("Impossible de transférer des fonds vers le même portefeuille");
        }

        Wallet fromWallet = getWalletEntityByPlayerId(fromPlayerId);
        Wallet toWallet = getWalletEntityByPlayerId(toPlayerId);

        if (!fromWallet.hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Solde insuffisant pour le transfert");
        }

        String reference = generateReference();
        BigDecimal fromBalanceBefore = fromWallet.getBalance();
        BigDecimal toBalanceBefore = toWallet.getBalance();

        // Debit from sender
        fromWallet.withdraw(amount);
        walletRepository.save(fromWallet);

        createTransaction(
                fromWallet,
                TransactionType.TRANSFER_OUT,
                amount,
                fromBalanceBefore,
                fromWallet.getBalance(),
                description != null ? description : "Transfert vers joueur #" + toPlayerId,
                reference
        );

        // Credit to receiver
        toWallet.deposit(amount);
        walletRepository.save(toWallet);

        createTransaction(
                toWallet,
                TransactionType.TRANSFER_IN,
                amount,
                toBalanceBefore,
                toWallet.getBalance(),
                description != null ? description : "Transfert depuis joueur #" + fromPlayerId,
                reference
        );

        logger.info("Transfer completed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(Long playerId, BigDecimal amount) {
        Wallet wallet = getWalletEntityByPlayerId(playerId);
        return wallet.hasSufficientBalance(amount);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long playerId) {
        Wallet wallet = getWalletEntityByPlayerId(playerId);
        return wallet.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDTO> getTransactionHistory(Long playerId) {
        Wallet wallet = getWalletEntityByPlayerId(playerId);
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        return transactions.stream()
                .map(this::convertTransactionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionDTO> getTransactionHistory(Long playerId, Pageable pageable) {
        Wallet wallet = getWalletEntityByPlayerId(playerId);
        Page<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
        return transactions.map(this::convertTransactionToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDTO> getTransactionsByType(Long playerId, TransactionType type) {
        Wallet wallet = getWalletEntityByPlayerId(playerId);
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdAndTypeOrderByCreatedAtDesc(wallet.getId(), type);
        return transactions.stream()
                .map(this::convertTransactionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDTO> getTransactionsBetweenDates(Long playerId, LocalDateTime startDate, LocalDateTime endDate) {
        Wallet wallet = getWalletEntityByPlayerId(playerId);
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                wallet.getId(), startDate, endDate);
        return transactions.stream()
                .map(this::convertTransactionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WalletTransactionDTO adminCredit(Long playerId, BigDecimal amount, String reason) {
        logger.info("Admin credit: {} to player {}", amount, playerId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du crédit doit être positif");
        }

        Wallet wallet = getWalletEntityByPlayerId(playerId);
        BigDecimal balanceBefore = wallet.getBalance();

        wallet.deposit(amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                wallet,
                TransactionType.ADMIN_CREDIT,
                amount,
                balanceBefore,
                wallet.getBalance(),
                reason != null ? reason : "Crédit administratif",
                generateReference()
        );

        return convertTransactionToDTO(transaction);
    }

    @Override
    public WalletTransactionDTO adminDebit(Long playerId, BigDecimal amount, String reason) {
        logger.info("Admin debit: {} from player {}", amount, playerId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du débit doit être positif");
        }

        Wallet wallet = getWalletEntityByPlayerId(playerId);
        BigDecimal balanceBefore = wallet.getBalance();

        // Admin can debit even if balance is insufficient (creates negative balance)
        wallet.withdraw(amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                wallet,
                TransactionType.ADMIN_DEBIT,
                amount,
                balanceBefore,
                wallet.getBalance(),
                reason != null ? reason : "Débit administratif",
                generateReference()
        );

        return convertTransactionToDTO(transaction);
    }

    @Override
    public void deleteWallet(Long playerId) {
        logger.info("Deleting wallet for player: {}", playerId);
        walletRepository.deleteByPlayerId(playerId);
    }

    // Helper methods

    private WalletTransaction createTransaction(Wallet wallet, TransactionType type, BigDecimal amount,
                                                 BigDecimal balanceBefore, BigDecimal balanceAfter,
                                                 String description, String reference) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .reference(reference)
                .build();

        return transactionRepository.save(transaction);
    }

    private String generateReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .playerId(wallet.getPlayer().getId())
                .playerUsername(wallet.getPlayer().getUsername())
                .playerEmail(wallet.getPlayer().getEmail())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .totalDeposits(wallet.getTotalDeposits())
                .totalWithdrawals(wallet.getTotalWithdrawals())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletTransactionDTO convertTransactionToDTO(WalletTransaction transaction) {
        return WalletTransactionDTO.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .reference(transaction.getReference())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

