package tn.esprit.piboursebackend.Credit.Entity;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
    @RequestMapping("/api/loans")

    public class LoanController {

        @Autowired
        private LoanService loanService;

        // 🆕 Créer un prêt pour un joueur donné
        @PostMapping("/create/{playerId}")
        public Loan createLoan(
                @PathVariable Long playerId,
                @RequestParam BigDecimal amount

        ) {
            return loanService.createLoan(playerId, amount);
        }

        // 📜 Récupérer tous les prêts
       @GetMapping("/all")
        public List<Loan> getAllLoans() {
            return loanService.getAllLoans();
        }


        @GetMapping("/player/{playerId}")
        public List<Loan> getLoansByPlayer(@PathVariable Long playerId) {
            return loanService.getLoansByPlayer(playerId);
        }





}
