package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Dto.NavBreakdown;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot;
import tn.esprit.piboursebackend.Portfolio.Entity.PortfolioSnapshot.PricingMode;
import tn.esprit.piboursebackend.Portfolio.service.NavService;

import java.time.Instant;

@RestController
@RequestMapping("/api/nav")
public class NavController {

    private final NavService navService;
    public NavController(NavService navService){ this.navService = navService; }

    @GetMapping("/compute")
    public ResponseEntity<NavBreakdown> compute(@RequestParam Long portfolioId,
                                                @RequestParam Instant asOf,
                                                @RequestParam(defaultValue="MARK_TO_MARKET") PricingMode pricingMode) {
        return ResponseEntity.ok(navService.computeNAV(portfolioId, asOf, pricingMode));
    }

    @PostMapping("/snapshot")
    public ResponseEntity<PortfolioSnapshot> snapshot(@RequestParam Long portfolioId,
                                                      @RequestParam Instant asOf,
                                                      @RequestParam(defaultValue="MARK_TO_MARKET") PricingMode pricingMode) {
        return ResponseEntity.ok(navService.takeSnapshot(portfolioId, asOf, pricingMode));
    }
}
