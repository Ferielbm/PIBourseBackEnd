package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Dto.*;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;
import tn.esprit.piboursebackend.Portfolio.service.PositionService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/positionsMa")
public class PositionMathController {

    private final PositionService svc;
    public PositionMathController(PositionService svc){ this.svc = svc; }

    @PostMapping("/apply-fill")
    public ResponseEntity<PositionView> applyFill(@RequestBody FillRequest req) {
        return ResponseEntity.ok(svc.applyFill(req));
    }

    @GetMapping("/{positionId}/mtm")
    public ResponseEntity<MtMResponse> markToMarket(@PathVariable Long positionId,
                                                    @RequestParam Instant asOf) {
        return ResponseEntity.ok(svc.markToMarket(positionId, asOf));
    }

    @GetMapping("/{positionId}/pnl")
    public ResponseEntity<PnlResponse> pnlForPosition(@PathVariable Long positionId,
                                                      @RequestParam Instant start,
                                                      @RequestParam Instant end,
                                                      @RequestParam(defaultValue="AVERAGE") CostMethod method) {
        return ResponseEntity.ok(svc.computePnLForPosition(positionId, start, end, method));
    }

    @GetMapping("/portfolio/{portfolioId}/pnl")
    public ResponseEntity<PnlResponse> pnlForPortfolio(@PathVariable Long portfolioId,
                                                       @RequestParam Instant start,
                                                       @RequestParam Instant end,
                                                       @RequestParam(defaultValue="AVERAGE") CostMethod method) {
        return ResponseEntity.ok(svc.computePnLForPortfolio(portfolioId, start, end, method));
    }

    @GetMapping("/{positionId}/lots")
    public ResponseEntity<List<LotView>> lots(@PathVariable Long positionId) {
        return ResponseEntity.ok(svc.lotManagement(positionId));
    }
}
