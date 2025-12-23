package tn.esprit.piboursebackend.Player.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Player.Entities.PlayerActivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerActivityRepository extends JpaRepository<PlayerActivity, Long> {

    Optional<PlayerActivity> findByPlayerId(Long playerId);

    /**
     * Trouver tous les joueurs actifs (lastActivity dans les X dernières minutes)
     */
    @Query("SELECT pa FROM PlayerActivity pa WHERE pa.lastActivity >= :since")
    List<PlayerActivity> findActivePlayers(@Param("since") LocalDateTime since);

    /**
     * Compter les joueurs actifs
     */
    @Query("SELECT COUNT(pa) FROM PlayerActivity pa WHERE pa.lastActivity >= :since")
    Long countActivePlayers(@Param("since") LocalDateTime since);

    /**
     * Trouver les joueurs connectés aujourd'hui
     */
    @Query("SELECT pa FROM PlayerActivity pa WHERE pa.lastLogin >= :startOfDay")
    List<PlayerActivity> findPlayersLoggedInToday(@Param("startOfDay") LocalDateTime startOfDay);
}
