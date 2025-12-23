package tn.esprit.piboursebackend.Player.Controllers;

import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.DTO.WalletResponseDTO;
import tn.esprit.piboursebackend.Player.Services.PlayerService;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;
import tn.esprit.piboursebackend.Order.Repository.WalletReservationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final WalletRepository walletRepository;
    private final WalletReservationRepository reservationRepository;

    public PlayerController(
        PlayerService playerService, 
        WalletRepository walletRepository,
        WalletReservationRepository reservationRepository
    ) {
        this.playerService = playerService;
        this.walletRepository = walletRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player);
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
    }

    @GetMapping("/{id}/wallet")
    public WalletResponseDTO getPlayerWallet(@PathVariable Long id) {
        Wallet wallet = walletRepository.findByPlayer_Id(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found for player " + id));
        
        // Calculer le montant réservé (réservations actives)
        BigDecimal reserved = reservationRepository.sumActiveRemainingByPlayerId(id);
        if (reserved == null) {
            reserved = BigDecimal.ZERO;
        }
        
        // Calculer le disponible
        BigDecimal available = wallet.getBalance().subtract(reserved);
        
        return WalletResponseDTO.builder()
                .id(wallet.getId())
                .playerId(id)
                .balance(wallet.getBalance())          // Solde total de la BDD
                .reserved(reserved)                    // Montant réservé
                .available(available)                  // Disponible = balance - reserved
                .currency("EUR")
                .lastUpdated(LocalDateTime.now().toString())
                .build();
    }

    @PostMapping("/bootstrap")
    public Player bootstrapTestPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player);
    }
}
