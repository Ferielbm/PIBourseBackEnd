package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Dto.ClonePortfolioRequest;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;
import tn.esprit.piboursebackend.Portfolio.service.PortfolioService;

import java.util.List;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService PortfolioService;

    public PortfolioController(PortfolioService PortfolioService) {
        this.PortfolioService = PortfolioService;
    }

    @GetMapping
    public List<Portfolio> getAllPortfolios() {
        return PortfolioService.getAllPortfolios();
    }

    @PostMapping
    public Portfolio createPortfolio(@RequestBody Portfolio Portfolio) {
        return PortfolioService.createPortfolio(Portfolio);
    }

    @DeleteMapping("/{id}")
    public void deletePortfolio(@PathVariable Long id) {
        PortfolioService.deletePortfolio(id);
    }
    @PostMapping("/{id}/archive")
    public ResponseEntity<Portfolio> archive(@PathVariable Long id) {
        return ResponseEntity.ok(PortfolioService.archivePortfolio(id));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<Portfolio> clone(@PathVariable Long id,
                                           @RequestBody(required = false) ClonePortfolioRequest req) {
        var asOf = (req == null ? java.time.Instant.now() : req.asOf());
        return ResponseEntity.ok(PortfolioService.clonePortfolio(id, asOf));
    }
}
