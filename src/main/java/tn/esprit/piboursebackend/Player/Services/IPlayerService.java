package tn.esprit.piboursebackend.Player.Services;

import tn.esprit.piboursebackend.Player.Entities.Player;
import java.util.List;
import java.util.UUID;

public interface IPlayerService {
    Player createPlayer(Player player);
    Player getPlayerById(UUID id);
    List<Player> getAllPlayers();
    Player updatePlayer(UUID id, Player player);
    void deletePlayer(UUID id);
}
