package tn.esprit.piboursebackend.Auth.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Auth.DTO.*;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Entities.Wallet;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.WalletRepository;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlayerRepository playerRepository;
    private final WalletRepository walletRepository;

    /**
     * Inscription d'un nouveau joueur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (playerRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Créer le joueur
        Player player = new Player();
        player.setUsername(request.getUsername());
        player.setEmail(request.getEmail());
        
        // TODO: En production, hasher le mot de passe avec BCrypt
        // player.setPassword(passwordEncoder.encode(request.getPassword()));
        player.setPassword(request.getPassword()); // Pour l'instant sans hashage
        
        // Définir le rôle (ROLE_PLAYER par défaut)
        player.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_PLAYER);
        
        player = playerRepository.save(player);
        log.info("✅ Nouveau joueur créé: {} ({})", player.getUsername(), player.getRole());

        // Créer le wallet avec un capital initial
        Wallet wallet = new Wallet();
        wallet.setPlayer(player);
        wallet.setBalance(BigDecimal.valueOf(10000.00)); // Capital initial de 10,000€
        walletRepository.save(wallet);
        
        log.info("💰 Wallet créé pour {}: {}€", player.getUsername(), wallet.getBalance());

        return AuthResponse.builder()
                .playerId(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .role(player.getRole())
                .message("Inscription réussie")
                .build();
    }

    /**
     * Connexion d'un joueur
     */
    public AuthResponse login(LoginRequest request) {
        Player player = playerRepository.findByEmail(request.getEmail());
        
        if (player == null) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // TODO: En production, vérifier le hash du mot de passe
        // if (!passwordEncoder.matches(request.getPassword(), player.getPassword()))
        if (!player.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        log.info("✅ Connexion réussie: {} ({})", player.getUsername(), player.getRole());

        return AuthResponse.builder()
                .playerId(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .role(player.getRole())
                .message("Connexion réussie")
                .build();
    }

    /**
     * Vérifier si un joueur a le rôle MENEUR_JEU
     */
    public boolean isGameMaster(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable"));
        return player.getRole() == Role.ROLE_MENEUR_JEU || player.getRole() == Role.ROLE_ADMIN;
    }
}
