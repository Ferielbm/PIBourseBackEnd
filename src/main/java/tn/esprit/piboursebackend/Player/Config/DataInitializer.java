package tn.esprit.piboursebackend.Player.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Entities.Transaction;
import tn.esprit.piboursebackend.Player.Entities.TransactionType;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.TransactionRepository;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;

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
                .password("admin123") // In production, this should be encrypted
                .role(Role.ROLE_ADMIN)
                .build();

        Player player1 = Player.builder()
                .username("john_trader")
                .email("john@example.com")
                .password("password123")
                .role(Role.ROLE_PLAYER)
                .build();

        Player player2 = Player.builder()
                .username("sarah_investor")
                .email("sarah@example.com")
                .password("password123")
                .role(Role.ROLE_PLAYER)
                .build();

        Player player3 = Player.builder()
                .username("mike_stocks")
                .email("mike@example.com")
                .password("password123")
                .role(Role.ROLE_PLAYER)
                .build();

        // Save players
        playerRepository.saveAll(Arrays.asList(admin, player1, player2, player3));
        log.info("Created {} players", 4);

        // Create Transactions for player1
        Transaction deposit1 = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(10000.0)
                .player(player1)
                .build();

        Transaction buy1 = Transaction.builder()
                .type(TransactionType.BUY)
                .amount(2500.0)
                .player(player1)
                .build();

        Transaction sell1 = Transaction.builder()
                .type(TransactionType.SELL)
                .amount(3000.0)
                .player(player1)
                .build();

        // Create Transactions for player2
        Transaction deposit2 = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(15000.0)
                .player(player2)
                .build();

        Transaction buy2 = Transaction.builder()
                .type(TransactionType.BUY)
                .amount(5000.0)
                .player(player2)
                .build();

        Transaction withdraw1 = Transaction.builder()
                .type(TransactionType.WITHDRAW)
                .amount(2000.0)
                .player(player2)
                .build();

        // Create Transactions for player3
        Transaction deposit3 = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(20000.0)
                .player(player3)
                .build();

        Transaction buy3 = Transaction.builder()
                .type(TransactionType.BUY)
                .amount(8000.0)
                .player(player3)
                .build();

        Transaction sell2 = Transaction.builder()
                .type(TransactionType.SELL)
                .amount(9000.0)
                .player(player3)
                .build();

        Transaction withdraw2 = Transaction.builder()
                .type(TransactionType.WITHDRAW)
                .amount(3000.0)
                .player(player3)
                .build();

        // Save transactions
        transactionRepository.saveAll(Arrays.asList(
                deposit1, buy1, sell1,
                deposit2, buy2, withdraw1,
                deposit3, buy3, sell2, withdraw2
        ));
        
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

