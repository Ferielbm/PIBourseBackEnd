package tn.esprit.piboursebackend.Player.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitaire pour la génération et la validation des tokens JWT
 * 
 * CORRECTION HS512 :
 * L'algorithme HS512 (HMAC-SHA512) nécessite une clé de signature d'au moins 512 bits (64 bytes).
 * Avec l'API JJWT 0.11.5, il faut utiliser Keys.hmacShaKeyFor() qui :
 * - Convertit la chaîne secrète en tableau de bytes
 * - Crée une clé SecretKey adaptée à l'algorithme HS512
 * - Garantit la sécurité cryptographique requise par le standard JWT
 * 
 * L'ancienne méthode signWith(SignatureAlgorithm.HS512, String) est dépréciée car elle 
 * ne garantissait pas la taille minimale de la clé, d'où l'erreur WeakKeyException.
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    /**
     * Génère une clé secrète sécurisée pour HS512 à partir du secret configuré
     * @return SecretKey compatible avec l'algorithme HS512
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token JWT à partir d'un username
     * Utilise l'algorithme HS512 avec une clé de 512 bits minimum
     * 
     * @param username le nom d'utilisateur
     * @return le token JWT généré
     */
    public String generateJwtToken(String username) {
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Nouvelle API avec SecretKey
                    .compact();
            
            logger.info("JWT token généré avec succès pour l'utilisateur: {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du token JWT pour {}: {}", username, e.getMessage());
            throw new RuntimeException("Impossible de générer le token JWT", e);
        }
    }

    /**
     * Extrait le username du token JWT
     * 
     * @param token le token JWT
     * @return le username extrait du token
     */
    public String getUserNameFromJwtToken(String token) {
        try {
            String username = Jwts.parserBuilder() // Nouvelle API parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            
            logger.debug("Username extrait du token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du username du token: {}", e.getMessage());
            throw new RuntimeException("Impossible d'extraire le username du token", e);
        }
    }

    /**
     * Valide le token JWT
     * Vérifie la signature, l'expiration et l'intégrité du token
     * 
     * @param token le token à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            
            logger.debug("Token JWT validé avec succès");
            return true;
            
        } catch (SignatureException e) {
            logger.error("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformé: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Les claims JWT sont vides: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("Erreur JWT générale: {}", e.getMessage());
        }
        
        return false;
    }
}

