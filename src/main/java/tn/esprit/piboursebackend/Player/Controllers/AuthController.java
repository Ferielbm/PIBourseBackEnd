package tn.esprit.piboursebackend.Player.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Security.JwtUtils;
import tn.esprit.piboursebackend.Player.Security.UserDetailsImpl;
import tn.esprit.piboursebackend.Player.Security.dto.JwtResponse;
import tn.esprit.piboursebackend.Player.Security.dto.LoginRequest;
import tn.esprit.piboursebackend.Player.Security.dto.MessageResponse;
import tn.esprit.piboursebackend.Player.Security.dto.SignupRequest;
import tn.esprit.piboursebackend.Player.DTOs.ForgotPasswordRequest;
import tn.esprit.piboursebackend.Player.DTOs.ResetPasswordRequest;
import tn.esprit.piboursebackend.Player.DTOs.ValidateTokenResponse;
import tn.esprit.piboursebackend.Player.Services.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Contrôleur pour la gestion de l'authentification
 * Endpoints: /api/auth/register et /api/auth/login
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API pour l'authentification et l'inscription")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Endpoint de connexion
     * POST /api/auth/login
     * @param loginRequest username et password
     * @return JWT token et informations utilisateur
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        
        try {
            // Utiliser email si fourni, sinon username
            String identifier = (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) 
                ? loginRequest.getEmail() 
                : loginRequest.getUsername();
                
            // Authentifier l'utilisateur via AuthenticationManager
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            // Si l'authentification échoue (mauvais username/password)
            return ResponseEntity
                    .status(401)
                    .body(new MessageResponse("Invalid username or password"));
        }

        // Mettre l'authentification dans le contexte Spring Security
        org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        // Récupérer les détails de l'utilisateur authentifié
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Générer le token JWT
        String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

        // Récupérer le rôle
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .get()
                .getAuthority();

        // Retourner la réponse avec le token
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role
        ));
    }

    /**
     * Endpoint d'inscription
     * POST /api/auth/register
     * @param signupRequest username, email, password et role
     * @return message de confirmation
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription utilisateur", description = "Crée un nouveau compte utilisateur")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        // Vérifier si le username existe déjà
        if (playerRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Vérifier si l'email existe déjà
        if (playerRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Créer un nouveau Player
        Player player = new Player();
        player.setUsername(signupRequest.getUsername());
        player.setEmail(signupRequest.getEmail());
        player.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        // Définir le rôle (par défaut ROLE_PLAYER si non spécifié)
        Role role = signupRequest.getRole();
        if (role == null) {
            role = Role.ROLE_PLAYER;
        }
        player.setRole(role);

        // Sauvegarder le player
        playerRepository.save(player);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    /**
     * Endpoint pour demander la réinitialisation du mot de passe
     * POST /api/auth/forgot-password
     * @param request contient l'email de l'utilisateur
     * @return message de confirmation
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Envoie un email avec un lien de réinitialisation")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.createPasswordResetToken(request.getEmail());
            return ResponseEntity.ok(new MessageResponse(
                "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new MessageResponse(
                "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation."
            ));
        }
    }

    /**
     * Endpoint pour valider un token de réinitialisation
     * GET /api/auth/validate-reset?token=xxx
     * @param token le token à valider
     * @return informations sur la validité du token
     */
    @GetMapping("/validate-reset")
    @Operation(summary = "Valider un token", description = "Vérifie si un token de réinitialisation est valide")
    public ResponseEntity<ValidateTokenResponse> validateResetToken(@RequestParam String token) {
        ValidateTokenResponse response = passwordResetService.validateResetToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour réinitialiser le mot de passe
     * POST /api/auth/reset-password
     * @param request contient le token et le nouveau mot de passe
     * @return message de confirmation
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Définit un nouveau mot de passe avec un token valide")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Une erreur est survenue lors de la réinitialisation du mot de passe."));
        }
    }
}

