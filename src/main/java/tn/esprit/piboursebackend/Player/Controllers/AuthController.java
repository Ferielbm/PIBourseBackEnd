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

/**
 * Contrôleur pour la gestion de l'authentification
 * Endpoints: /api/auth/register et /api/auth/login
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Endpoint de connexion
     * POST /api/auth/login
     * @param loginRequest username et password
     * @return JWT token et informations utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        
        try {
            // Authentifier l'utilisateur via AuthenticationManager
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
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
}

