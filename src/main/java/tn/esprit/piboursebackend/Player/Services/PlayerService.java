package tn.esprit.piboursebackend.Player.Services;

import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final WalletRepository walletRepository;

    public PlayerService(PlayerRepository playerRepository, WalletRepository walletRepository) {
        this.playerRepository = playerRepository;
        this.walletRepository = walletRepository;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player createPlayer(Player player) {
        // Sauvegarde le joueur d'abord
        Player savedPlayer = playerRepository.save(player);
        
        // Crée automatiquement un wallet pour le joueur
        Wallet wallet = Wallet.builder()
                .player(savedPlayer)
                .balance(BigDecimal.valueOf(10000)) // Solde initial de 10 000
                .totalDeposits(BigDecimal.valueOf(10000))
                .totalWithdrawals(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);
        
        return savedPlayer;
    }

    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }
}
