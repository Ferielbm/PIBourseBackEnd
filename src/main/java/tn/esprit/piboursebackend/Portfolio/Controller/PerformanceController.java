package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.service.PerformanceService;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService perf;
    public PerformanceController(PerformanceService p){ this.perf = p; }

    @GetMapping("/twr")
    public ResponseEntity<BigDecimal> twr(@RequestParam Long portfolioId,
                                          @RequestParam Instant start,
                                          @RequestParam Instant end) {
        return ResponseEntity.ok(perf.performanceTWR(portfolioId, start, end));
    }

    @GetMapping("/mwr")
    public ResponseEntity<BigDecimal> mwr(@RequestParam Long portfolioId,
                                          @RequestParam Instant start,
                                          @RequestParam Instant end) {
        return ResponseEntity.ok(perf.performanceMWR(portfolioId, start, end));
    }
}
