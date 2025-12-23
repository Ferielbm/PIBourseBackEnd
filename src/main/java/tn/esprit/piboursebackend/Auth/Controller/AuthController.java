package tn.esprit.piboursebackend.Auth.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Auth.DTO.*;
import tn.esprit.piboursebackend.Auth.Service.AuthService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inscription
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur inscription:", e);
            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .message("Erreur: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Connexion
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur connexion:", e);
            return ResponseEntity.badRequest().body(
                AuthResponse.builder()
                    .message("Erreur: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Vérifier si un joueur est meneur de jeu
     */
    @GetMapping("/is-game-master/{playerId}")
    public ResponseEntity<Boolean> isGameMaster(@PathVariable Long playerId) {
        try {
            boolean isGM = authService.isGameMaster(playerId);
            return ResponseEntity.ok(isGM);
        } catch (Exception e) {
            log.error("❌ Erreur vérification rôle:", e);
            return ResponseEntity.ok(false);
        }
    }
}
