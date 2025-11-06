package tn.esprit.piboursebackend.Player.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.esprit.piboursebackend.Player.Services.OAuth2UserService;

/**
 * Configuration de sécurité Spring Security 6
 * - Endpoints publics : /api/auth/**, /swagger-ui/**, /v3/api-docs/**
 * - Endpoints protégés par JWT et rôles
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private OAuth2UserService oauth2UserService;

    /**
     * Provider d'authentification avec UserDetailsService et PasswordEncoder
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager pour valider username/password
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration de la chaîne de sécurité
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (API REST stateless)
                .csrf(csrf -> csrf.disable())
                
                // Gestion des erreurs d'authentification
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                
                // Session STATELESS (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Autorisation des requêtes
                .authorizeHttpRequests(auth -> auth
                        // ✅ Endpoints publics (pas d'authentification requise)
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()
                        
                        // ✅ Endpoints protégés par rôle
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/player/**").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/game-master/**").hasAnyRole("GAME_MASTER", "ADMIN")
                        
                        // ✅ Tous les autres endpoints nécessitent une authentification
                        .anyRequest().authenticated()
                )
                
                // Configuration OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService)
                        )
                        .defaultSuccessUrl("/oauth2/callback/google", true)
                        .failureUrl("/oauth2/callback/google?error=true")
                )
                
                // Désactiver frameOptions pour H2 Console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        // Ajouter le provider d'authentification
        http.authenticationProvider(authenticationProvider());

        // Ajouter le filtre JWT avant UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
