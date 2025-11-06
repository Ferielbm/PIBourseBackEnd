package tn.esprit.piboursebackend.Player.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller pour gérer l'authentification OAuth2 avec Google
 * - Endpoint de redirection après authentification Google
 * - Génération de JWT pour l'utilisateur connecté
 */
@RestController
@RequestMapping("/oauth2")
@Tag(name = "OAuth2 Authentication", description = "Gestion de l'authentification OAuth2 avec Google")
public class OAuth2Controller {


    /**
     * Endpoint de callback après authentification Google
     * Redirige vers le frontend avec le JWT en paramètre
     */
    @GetMapping("/callback/google")
    @Operation(summary = "Callback Google OAuth2", 
               description = "Endpoint appelé par Google après authentification. Génère un JWT et redirige vers le frontend.")
    public void googleCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                
                // Récupérer le JWT généré par le service OAuth2
                String jwt = (String) oauth2User.getAttributes().get("jwt");
                Long playerId = (Long) oauth2User.getAttributes().get("playerId");
                String playerRole = (String) oauth2User.getAttributes().get("playerRole");
                
                if (jwt != null) {
                    // Redirection vers le frontend avec le JWT
                    String frontendUrl = "http://localhost:4200/auth/callback?token=" + jwt + 
                                       "&playerId=" + playerId + 
                                       "&role=" + playerRole;
                    response.sendRedirect(frontendUrl);
                } else {
                    // Erreur - redirection vers la page de connexion avec un message d'erreur
                    response.sendRedirect("http://localhost:4200/auth/login?error=oauth2_failed");
                }
            } else {
                // Pas d'authentification - redirection vers la page de connexion
                response.sendRedirect("http://localhost:4200/auth/login?error=authentication_failed");
            }
        } catch (Exception e) {
            // Erreur lors du traitement - redirection vers la page de connexion avec un message d'erreur
            response.sendRedirect("http://localhost:4200/auth/login?error=oauth2_error");
        }
    }

    /**
     * Endpoint pour obtenir les informations de l'utilisateur connecté via OAuth2
     * Utile pour le frontend pour récupérer les informations après redirection
     */
    @GetMapping("/user")
    @Operation(summary = "Informations utilisateur OAuth2", 
               description = "Récupère les informations de l'utilisateur connecté via OAuth2")
    public ResponseEntity<Map<String, Object>> getOAuth2User() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("jwt", oauth2User.getAttributes().get("jwt"));
                userInfo.put("playerId", oauth2User.getAttributes().get("playerId"));
                userInfo.put("playerRole", oauth2User.getAttributes().get("playerRole"));
                userInfo.put("email", oauth2User.getAttributes().get("email"));
                userInfo.put("name", oauth2User.getAttributes().get("name"));
                userInfo.put("picture", oauth2User.getAttributes().get("picture"));
                
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non authentifié via OAuth2"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des informations utilisateur"));
        }
    }

    /**
     * Endpoint pour tester la redirection OAuth2
     * Redirige vers l'URL d'autorisation Google
     */
    @GetMapping("/authorization/google")
    @Operation(summary = "Déclencher l'authentification Google", 
               description = "Redirige vers Google pour l'authentification OAuth2")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        // Cette redirection sera gérée par Spring Security OAuth2
        response.sendRedirect("/oauth2/authorization/google");
    }
}
