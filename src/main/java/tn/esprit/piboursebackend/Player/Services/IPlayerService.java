package tn.esprit.piboursebackend.Player.Services;

import tn.esprit.piboursebackend.Player.Entities.Player;

import java.util.List;

public interface IPlayerService {
    List<Player> getAllPlayers();
    Player getPlayerById(Long id);
    Player createPlayer(Player player);
    Player updatePlayer(Long id, Player player);
    void deletePlayer(Long id);
    Player getPlayerByEmail(String email);
}
