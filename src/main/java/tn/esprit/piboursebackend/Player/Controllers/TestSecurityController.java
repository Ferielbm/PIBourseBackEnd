package tn.esprit.piboursebackend.Player.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur de test pour démontrer l'autorisation basée sur les rôles
 */
@RestController
@RequestMapping("/api")
public class TestSecurityController {

    /**
     * Endpoint accessible uniquement aux ADMIN
     */
    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminAccess() {
        return ResponseEntity.ok("✅ Admin Board: Vous avez accès en tant qu'ADMIN");
    }

    /**
     * Endpoint accessible aux PLAYER et ADMIN
     */
    @GetMapping("/player/test")
    @PreAuthorize("hasAnyRole('PLAYER', 'ADMIN')")
    public ResponseEntity<String> playerAccess() {
        return ResponseEntity.ok("✅ Player Board: Vous avez accès en tant que PLAYER ou ADMIN");
    }

    /**
     * Endpoint accessible à tous les utilisateurs authentifiés
     */
    @GetMapping("/user/test")
    public ResponseEntity<String> userAccess() {
        return ResponseEntity.ok("✅ User Content: Accessible à tous les utilisateurs authentifiés");
    }
}

