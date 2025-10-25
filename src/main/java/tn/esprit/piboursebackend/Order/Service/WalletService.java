// src/main/java/tn/esprit/piboursebackend/Order/Service/WalletService.java
package tn.esprit.piboursebackend.Order.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tn.esprit.piboursebackend.Order.Entity.WalletReservation;
import tn.esprit.piboursebackend.Order.Entity.WalletReservationStatus;
import tn.esprit.piboursebackend.Order.Repository.WalletReservationRepository;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepo;
    private final WalletReservationRepository reservationRepo;

    /** Solde réellement dispo = balance - réservations actives */
    @Transactional
    public BigDecimal getAvailable(Long playerId){
        Wallet w = walletRepo.findByPlayer_Id(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        BigDecimal reserved = reservationRepo.sumActiveRemainingByPlayerId(playerId);
        return w.getBalance().subtract(reserved);
    }

    /** Réserver (ex: BUY LIMIT price*qty) */
    @Transactional
    public WalletReservation reserve(Long playerId, Long orderId, BigDecimal amount, String reason){
        if (amount == null || amount.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reserve amount must be > 0");

        // lock wallet
        Wallet w = walletRepo.findByPlayer_Id(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));

        BigDecimal available = w.getBalance().subtract(reservationRepo.sumActiveRemainingByPlayerId(playerId));
        if (available.compareTo(amount) < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");

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

    /** Consommer la réservation (ex: trade exécuté) */
    @Transactional
    public void consume(Long orderId, BigDecimal amount){
        if (amount == null || amount.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "consume amount must be > 0");

        // lock réservations actives de l’ordre
        var actives = reservationRepo.lockAllActiveByOrderId(orderId);
        if (actives.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no active reservation for order "+orderId);

        BigDecimal remaining = amount;
        for (var r : actives){
            if (remaining.signum() <= 0) break;
            BigDecimal take = r.getRemainingAmount().min(remaining);
            r.setRemainingAmount(r.getRemainingAmount().subtract(take));
            remaining = remaining.subtract(take);
            if (r.getRemainingAmount().signum() == 0){
                r.setStatus(WalletReservationStatus.CONSUMED);
            }
            reservationRepo.save(r);
        }
        if (remaining.signum() > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "reservation underflow");
    }

    /** Libérer intégralement (ex: annulation d’ordre) */
    @Transactional
    public void releaseAllForOrder(Long orderId){
        var actives = reservationRepo.lockAllActiveByOrderId(orderId);
        for (var r : actives){
            r.setStatus(WalletReservationStatus.RELEASED);
            r.setRemainingAmount(BigDecimal.ZERO);
            reservationRepo.save(r);
        }
    }

    /** Créditer/Débiter cash (virement final vers vendeur) */
    @Transactional
    public void transfer(Long fromPlayerId, Long toPlayerId, BigDecimal amount){
        if (amount == null || amount.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transfer amount must be > 0");

        Wallet from = walletRepo.findByPlayerIdForUpdate(fromPlayerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found: buyer"));
        Wallet to = walletRepo.findByPlayerIdForUpdate(toPlayerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found: seller"));

        // Ici on suppose que le “cash réel” est déjà réservé côté acheteur,
        // donc on débite seulement du “réservé” (consume) puis on crédite le vendeur.
        // Si tu veux impacter balance directement, décommente :
        // if (from.getBalance().compareTo(amount) < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"insufficient");
        // from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        // NB: le débit réel du buyer est implicitement “effectué” par la consommation de réservation + éventuellement
        // un ajustement du solde si tu veux le matérialiser (à toi de décider la stratégie comptable).
        walletRepo.save(from);
        walletRepo.save(to);
    }
}
