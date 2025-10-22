package tn.esprit.piboursebackend.Player.Security;

import java.util.Arrays;
import java.util.List;

/**
 * Liste des endpoints publics qui ne nécessitent pas d'authentification
 */
public class PublicEndpoints {
    
    public static final List<String> PUBLIC_URLS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );
    
    /**
     * Vérifie si un path est public
     */
    public static boolean isPublic(String path) {
        return PUBLIC_URLS.stream().anyMatch(path::startsWith);
    }
}

