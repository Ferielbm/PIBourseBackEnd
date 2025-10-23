package tn.esprit.piboursebackend.Player.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Player.DTOs.ValidateTokenResponse;
import tn.esprit.piboursebackend.Player.Entities.PasswordResetToken;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Repositories.PasswordResetTokenRepository;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PlayerRepository playerRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-expiration-hours:1}")
    private int tokenExpirationHours;

    @Value("${app.password-reset.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Crée un token de réinitialisation et envoie un email
     */
    @Transactional
    public void createPasswordResetToken(String email) {
        Player player = playerRepository.findByEmail(email);
        
        if (player == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Pour des raisons de sécurité, on ne révèle pas si l'email existe ou non
            return;
        }

        // Générer un token unique
        String token = UUID.randomUUID().toString();

        // Calculer la date d'expiration (1 heure par défaut)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenExpirationHours);

        // Créer et sauvegarder le token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .player(player)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Envoyer l'email
        sendPasswordResetEmail(player.getEmail(), token);

        log.info("Password reset token created for user: {}", email);
    }

    /**
     * Envoie l'email de réinitialisation
     */
    private void sendPasswordResetEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Réinitialisation de votre mot de passe - PiBourse");
            
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            
            message.setText(
                "Bonjour,\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :\n" +
                resetLink + "\n\n" +
                "Ce lien expirera dans " + tokenExpirationHours + " heure(s).\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe PiBourse"
            );

            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Valide un token de réinitialisation
     */
    public ValidateTokenResponse validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null) {
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Token invalide")
                    .build();
        }

        if (resetToken.isUsed()) {
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Ce token a déjà été utilisé")
                    .build();
        }

        if (resetToken.isExpired()) {
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Ce token a expiré")
                    .build();
        }

        return ValidateTokenResponse.builder()
                .valid(true)
                .message("Token valide")
                .email(resetToken.getPlayer().getEmail())
                .build();
    }

    /**
     * Réinitialise le mot de passe avec un token valide
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Ce token a déjà été utilisé");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Ce token a expiré");
        }

        // Mettre à jour le mot de passe
        Player player = resetToken.getPlayer();
        player.setPassword(passwordEncoder.encode(newPassword));
        playerRepository.save(player);

        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", player.getEmail());
    }

    /**
     * Nettoie les tokens expirés (à appeler périodiquement)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        log.info("Expired password reset tokens cleaned up");
    }
}

