package tn.esprit.piboursebackend.Player.Repositories;

import tn.esprit.piboursebackend.Player.Entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByEmail(String email);
}
