package tn.esprit.piboursebackend.Player.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT pour l'authentification
 * N'est PAS appliqué aux endpoints publics (/api/auth/**, /swagger-ui/**, etc.)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Détermine si le filtre doit être ignoré pour cette requête
     * Utilise getRequestURI() pour plus de fiabilité
     * @return true si le filtre doit être ignoré (endpoints publics)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        boolean shouldSkip = path.startsWith("/api/auth") || 
                             path.startsWith("/v3/api-docs") || 
                             path.startsWith("/swagger-ui") ||
                             path.contains("/h2-console");
        
        // Log pour debugging
        if (shouldSkip) {
            logger.debug("🔓 JWT Filter SKIPPED for: {} (public endpoint)", path);
        } else {
            logger.debug("🔒 JWT Filter APPLIED for: {}", path);
        }
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtils.getUserNameFromJwtToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtils.validateJwtToken(token)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        logger.debug("✅ JWT Authentication successful for user: {}", username);
                    } else {
                        logger.warn("⚠️ JWT Token validation failed");
                    }
                }
            } else {
                logger.debug("⚠️ No Bearer token found in Authorization header for: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("❌ Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
