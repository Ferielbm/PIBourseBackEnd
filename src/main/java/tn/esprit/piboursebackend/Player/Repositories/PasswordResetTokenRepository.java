package tn.esprit.piboursebackend.Player.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Player.Entities.PasswordResetToken;
import tn.esprit.piboursebackend.Player.Entities.Player;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Trouve un token par sa valeur
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Trouve tous les tokens d'un player
     */
    Optional<PasswordResetToken> findByPlayer(Player player);

    /**
     * Supprime tous les tokens expirés
     */
    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    /**
     * Vérifie si un token existe et n'est pas utilisé
     */
    boolean existsByTokenAndUsedFalse(String token);
}

