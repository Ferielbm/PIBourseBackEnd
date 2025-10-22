package tn.esprit.piboursebackend.Player.Services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class PlayerService implements IPlayerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    
    private final PlayerRepository playerRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player createPlayer(Player player) {
        logger.info("Creating player: {}", player.getUsername());
        Player savedPlayer = playerRepository.save(player);
        entityManager.flush();
        logger.info("Player created successfully with ID: {}", savedPlayer.getId());
        return savedPlayer;
    }

    @Override
    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Player updatePlayer(Long id, Player player) {
        logger.info("Updating player with ID: {}", id);
        Player existingPlayer = getPlayerById(id);
        existingPlayer.setUsername(player.getUsername());
        existingPlayer.setEmail(player.getEmail());
        existingPlayer.setPassword(player.getPassword());
        existingPlayer.setRole(player.getRole());
        Player updatedPlayer = playerRepository.save(existingPlayer);
        entityManager.flush();
        logger.info("Player updated successfully");
        return updatedPlayer;
    }

    @Override
    public void deletePlayer(Long id) {
        logger.info("Deleting player with ID: {}", id);
        playerRepository.deleteById(id);
        entityManager.flush();
        logger.info("Player deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Player getPlayerByEmail(String email) {
        return playerRepository.findByEmail(email);
    }
}
