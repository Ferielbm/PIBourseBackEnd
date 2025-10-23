package tn.esprit.piboursebackend.Player.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (playerRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        log.info("Initializing database with test data...");

        // Create Players
        Player admin = Player.builder()
                .username("admin")
                .email("admin@pibourse.tn")
                .password(passwordEncoder.encode("admin123")) // Encrypt password
                .role(Role.ROLE_ADMIN)
                .build();

        Player player1 = Player.builder()
                .username("john_trader")
                .email("john@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_PLAYER)
                .build();

        Player player2 = Player.builder()
                .username("sarah_investor")
                .email("sarah@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_PLAYER)
                .build();

        Player player3 = Player.builder()
                .username("mike_stocks")
                .email("mike@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_PLAYER)
                .build();

        // Save players
        playerRepository.saveAll(Arrays.asList(admin, player1, player2, player3));
        log.info("Created {} players", 4);


        
        log.info("Created {} transactions", 10);
        log.info("Database initialization completed successfully!");
        log.info("-------------------------------------------");
        log.info("Test credentials:");
        log.info("Admin - Email: admin@pibourse.tn, Password: admin123");
        log.info("Player 1 - Email: john@example.com, Password: password123");
        log.info("Player 2 - Email: sarah@example.com, Password: password123");
        log.info("Player 3 - Email: mike@example.com, Password: password123");
        log.info("-------------------------------------------");
    }
}

