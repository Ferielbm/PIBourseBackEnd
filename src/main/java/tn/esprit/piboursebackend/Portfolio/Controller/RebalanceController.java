package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Dto.*;
import tn.esprit.piboursebackend.Portfolio.Entity.TargetWeight;
import tn.esprit.piboursebackend.Portfolio.service.RebalanceService;

import java.util.List;

@RestController
@RequestMapping("/rebalance")
public class RebalanceController {

    private final RebalanceService svc;

    public RebalanceController(RebalanceService svc) { this.svc = svc; }

    @PostMapping("/targets")
    public ResponseEntity<List<TargetWeight>> setTargets(@RequestBody SetTargetWeightsRequest req) {
        return ResponseEntity.ok(svc.setTargetWeights(req));
    }

    @PostMapping("/propose")
    public ResponseEntity<List<TradeProposal>> propose(@RequestBody ProposeRebalanceRequest req) {
        return ResponseEntity.ok(svc.proposeRebalanceTrades(req));
    }

    @PostMapping("/apply-model")
    public ResponseEntity<List<TradeProposal>> applyModel(@RequestBody ApplyModelPortfolioRequest req) {
        return ResponseEntity.ok(svc.applyModelPortfolio(req));
    }
}
