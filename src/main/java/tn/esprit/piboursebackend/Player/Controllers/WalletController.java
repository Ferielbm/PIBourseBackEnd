package tn.esprit.piboursebackend.Player.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Player.DTOs.*;
import tn.esprit.piboursebackend.Player.Entities.WalletTransaction.TransactionType;
import tn.esprit.piboursebackend.Player.Security.UserDetailsImpl;
import tn.esprit.piboursebackend.Player.Security.dto.MessageResponse;
import tn.esprit.piboursebackend.Player.Services.IWalletService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Wallet", description = "API de gestion des portefeuilles")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    @Autowired
    private IWalletService walletService;

    /**
     * Get current user's wallet
     */
    @GetMapping("/my-wallet")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir mon portefeuille", description = "Récupère les informations du portefeuille de l'utilisateur connecté")
    public ResponseEntity<WalletDTO> getMyWallet(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        WalletDTO wallet = walletService.getWalletByPlayerId(userDetails.getId());
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get wallet by player ID (Admin only)
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Obtenir le portefeuille d'un joueur", description = "Admin uniquement - Récupère le portefeuille d'un joueur spécifique")
    public ResponseEntity<WalletDTO> getWalletByPlayerId(@PathVariable Long playerId) {
        WalletDTO wallet = walletService.getWalletByPlayerId(playerId);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get wallet balance
     */
    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir le solde", description = "Récupère le solde actuel du portefeuille")
    public ResponseEntity<BigDecimal> getBalance(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        BigDecimal balance = walletService.getBalance(userDetails.getId());
        return ResponseEntity.ok(balance);
    }

    /**
     * Deposit funds
     */
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Effectuer un dépôt", description = "Ajoute des fonds au portefeuille")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            WalletTransactionDTO transaction = walletService.deposit(
                    userDetails.getId(),
                    request.getAmount(),
                    request.getDescription()
            );
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Withdraw funds
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Effectuer un retrait", description = "Retire des fonds du portefeuille")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawRequest request, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            WalletTransactionDTO transaction = walletService.withdraw(
                    userDetails.getId(),
                    request.getAmount(),
                    request.getDescription()
            );
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Transfer funds to another player
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Effectuer un transfert", description = "Transfère des fonds vers un autre joueur")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            walletService.transfer(
                    userDetails.getId(),
                    request.getRecipientPlayerId(),
                    request.getAmount(),
                    request.getDescription()
            );
            return ResponseEntity.ok(new MessageResponse("Transfert effectué avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Check if user has sufficient balance
     */
    @GetMapping("/check-balance")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Vérifier le solde", description = "Vérifie si le solde est suffisant pour un montant donné")
    public ResponseEntity<Boolean> checkBalance(@RequestParam BigDecimal amount, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean sufficient = walletService.hasSufficientBalance(userDetails.getId(), amount);
        return ResponseEntity.ok(sufficient);
    }

    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir l'historique des transactions", description = "Récupère toutes les transactions du portefeuille")
    public ResponseEntity<List<WalletTransactionDTO>> getTransactionHistory(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<WalletTransactionDTO> transactions = walletService.getTransactionHistory(userDetails.getId());
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get paginated transaction history
     */
    @GetMapping("/transactions/paginated")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir l'historique paginé", description = "Récupère les transactions avec pagination")
    public ResponseEntity<Page<WalletTransactionDTO>> getTransactionHistoryPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransactionDTO> transactions = walletService.getTransactionHistory(userDetails.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by type
     */
    @GetMapping("/transactions/type/{type}")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir les transactions par type", description = "Filtre les transactions par type")
    public ResponseEntity<List<WalletTransactionDTO>> getTransactionsByType(
            @PathVariable TransactionType type,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<WalletTransactionDTO> transactions = walletService.getTransactionsByType(userDetails.getId(), type);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions between dates
     */
    @GetMapping("/transactions/date-range")
    @PreAuthorize("hasAnyRole('ROLE_PLAYER', 'ROLE_ADMIN')")
    @Operation(summary = "Obtenir les transactions par période", description = "Filtre les transactions entre deux dates")
    public ResponseEntity<List<WalletTransactionDTO>> getTransactionsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<WalletTransactionDTO> transactions = walletService.getTransactionsBetweenDates(
                userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Admin: Credit a player's wallet
     */
    @PostMapping("/admin/credit/{playerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Créditer un portefeuille", description = "Admin uniquement - Ajoute un crédit administratif")
    public ResponseEntity<?> adminCredit(
            @PathVariable Long playerId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {
        try {
            WalletTransactionDTO transaction = walletService.adminCredit(playerId, amount, reason);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Admin: Debit a player's wallet
     */
    @PostMapping("/admin/debit/{playerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Débiter un portefeuille", description = "Admin uniquement - Effectue un débit administratif")
    public ResponseEntity<?> adminDebit(
            @PathVariable Long playerId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {
        try {
            WalletTransactionDTO transaction = walletService.adminDebit(playerId, amount, reason);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Admin: Get transaction history of any player
     */
    @GetMapping("/admin/transactions/{playerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Obtenir l'historique d'un joueur", description = "Admin uniquement - Récupère les transactions d'un joueur")
    public ResponseEntity<List<WalletTransactionDTO>> getPlayerTransactionHistory(@PathVariable Long playerId) {
        List<WalletTransactionDTO> transactions = walletService.getTransactionHistory(playerId);
        return ResponseEntity.ok(transactions);
    }
}

