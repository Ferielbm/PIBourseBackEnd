package tn.esprit.piboursebackend.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API stateless
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Autorisations
                .authorizeHttpRequests(auth -> auth
                        // 👉 tes endpoints métier (playerId dans le path)
                        .requestMatchers("/api/players/**").permitAll()
                        // 👉 Swagger / OpenAPI (utile pour tester)
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/v3/api-docs.yaml"
                        ).permitAll()
                        // 👉 (optionnel) Actuator si tu l’utilises
                        .requestMatchers("/actuator/**").permitAll()
                        // le reste aussi ouvert (ou mets .authenticated() si besoin)
                        .anyRequest().permitAll()
                )

                // pas de login form / basic pour ces tests
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());

        return http.build();
    }
}