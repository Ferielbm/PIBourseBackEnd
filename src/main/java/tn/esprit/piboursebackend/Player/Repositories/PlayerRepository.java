package tn.esprit.piboursebackend.Player.Repositories;

import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByEmail(String email);
    
    // Trouver tous les joueurs (exclure les meneurs de jeu)
    List<Player> findByRole(Role role);
}
