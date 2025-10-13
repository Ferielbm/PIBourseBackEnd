package com.example.pibourse.Services;

import com.example.pibourse.Entities.Player;
import java.util.List;
import java.util.UUID;

public interface IPlayerService {
    Player createPlayer(Player player);
    Player getPlayerById(UUID id);
    List<Player> getAllPlayers();
    Player updatePlayer(UUID id, Player player);
    void deletePlayer(UUID id);
}
