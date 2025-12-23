package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.piboursebackend.Order.Entity.WalletReservation;
import tn.esprit.piboursebackend.Order.Entity.WalletReservationStatus;
import tn.esprit.piboursebackend.Order.Repository.WalletReservationRepository;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepo;
    private final WalletReservationRepository reservationRepo;
    private final PlayerRepository playerRepo;

    private BigDecimal safeActiveReserved(Long playerId) {
        BigDecimal reserved = reservationRepo.sumActiveRemainingByPlayerId(playerId);
        return reserved != null ? reserved : BigDecimal.ZERO;
    }

    /**
     * Solde réellement dispo = balance - réservations actives
     */
    @Transactional(readOnly = true)
    public BigDecimal getAvailable(Long playerId) {
        Wallet w = walletRepo.findByPlayer_Id(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));

        BigDecimal reserved = safeActiveReserved(playerId);
        return w.getBalance().subtract(reserved);
    }

    /**
     * Réserver (ex: BUY LIMIT price * qty)
     */
    @Transactional
    public WalletReservation reserve(Long playerId, Long orderId, BigDecimal amount, String reason) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reserve amount must be > 0");
        }

        // 🔒 On verrouille le wallet en écriture pour éviter les races
        Wallet w = walletRepo.findByPlayerIdForUpdate(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));

        BigDecimal reserved = safeActiveReserved(playerId);
        BigDecimal available = w.getBalance().subtract(reserved);

        if (available.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");
        }

        WalletReservation r = WalletReservation.builder()
                .playerId(playerId)
                .orderId(orderId)
                .amountReserved(amount)
                .remainingAmount(amount)
                .status(WalletReservationStatus.ACTIVE)
                .reason(reason)
                .build();

        return reservationRepo.save(r);
    }

    /**
     * Consommer la réservation (ex: trade exécuté) – on consomme le montant dans les réservations
     */
    @Transactional
    public void consume(Long orderId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "consume amount must be > 0");
        }

        // 🔒 On lock les réservations actives de l’ordre
        var actives = reservationRepo.lockAllActiveByOrderId(orderId);
        if (actives.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no active reservation for order " + orderId);
        }

        BigDecimal remaining = amount;

        for (WalletReservation r : actives) {
            if (remaining.signum() <= 0) break;

            BigDecimal take = r.getRemainingAmount().min(remaining);
            if (take.signum() <= 0) {
                continue;
            }

            r.setRemainingAmount(r.getRemainingAmount().subtract(take));
            remaining = remaining.subtract(take);

            if (r.getRemainingAmount().signum() == 0) {
                r.setStatus(WalletReservationStatus.CONSUMED);
            }

            reservationRepo.save(r);
        }

        if (remaining.signum() > 0) {
            // On a voulu consommer plus que ce qui est réservé
            throw new ResponseStatusException(HttpStatus.CONFLICT, "reservation underflow");
        }
    }

    /**
     * Libérer intégralement (ex: annulation d’ordre)
     */
    @Transactional
    public void releaseAllForOrder(Long orderId) {
        var actives = reservationRepo.lockAllActiveByOrderId(orderId);
        for (WalletReservation r : actives) {
            r.setStatus(WalletReservationStatus.RELEASED);
            r.setRemainingAmount(BigDecimal.ZERO);
            reservationRepo.save(r);
        }
    }

    /**
     * Créditer le vendeur après exécution d'un trade
     * IMPORTANT: l'acheteur a déjà "payé" via la réservation consommée (consume)
     * Cette méthode ne doit pas toucher aux réservations et ne sert qu'à créditer le vendeur.
     */
    @Transactional
    public void transfer(Long buyerId, Long sellerId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transfer amount must be > 0");
        }

        // On verrouille seulement le wallet du vendeur pour le créditer
        Wallet seller = walletRepo.findByPlayerIdForUpdate(sellerId).orElse(null);

        if (seller == null) {
            System.err.println("⚠️ Wallet non trouvé pour le vendeur (playerId=" + sellerId + "). Création automatique...");
            Player sellerPlayer = playerRepo.findById(sellerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found: " + sellerId));
            seller = Wallet.builder()
                    .player(sellerPlayer)
                    .balance(BigDecimal.ZERO)
                    .totalDeposits(BigDecimal.ZERO)
                    .totalWithdrawals(BigDecimal.ZERO)
                    .build();
            seller = walletRepo.save(seller);
            System.out.println("✅ Wallet créé pour le vendeur (playerId=" + sellerId + ")");
        }

        // 💰 Créditer uniquement le vendeur — le débit acheteur est pris en compte via la réservation consommée
        seller.setBalance(seller.getBalance().add(amount));
        walletRepo.save(seller);
        System.out.println("💸 Transfer crédit vendeur: " + amount + "€ -> seller=" + sellerId);
    }
}